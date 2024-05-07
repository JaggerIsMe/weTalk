package com.weTalk.controller;

import com.weTalk.annotation.GlobalInterceptor;
import com.weTalk.config.AppConfig;
import com.weTalk.dto.MessageSendDto;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.ResponseCodeEnum;
import com.weTalk.entity.po.ChatMessage;
import com.weTalk.entity.vo.ResponseVO;
import com.weTalk.exception.BusinessException;
import com.weTalk.service.ChatMessageService;
import com.weTalk.service.ChatSessionUserService;
import com.weTalk.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/chat")
public class ChatController extends ABaseController {

    public static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private AppConfig appConfig;

    /**
     * 发送普通文本类型消息和媒体类型消息
     *
     * @param request
     * @param contactId
     * @param messageContent
     * @param messageType
     * @param fileSize
     * @param fileName
     * @param fileType
     * @return
     */
    @RequestMapping("/sendMessage")
    @GlobalInterceptor
    public ResponseVO sendMessage(HttpServletRequest request, @NotEmpty String contactId,
                                  @NotEmpty @Max(500) String messageContent,
                                  @NotNull Integer messageType,
                                  Long fileSize,
                                  String fileName,
                                  Integer fileType) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setMessageType(messageType);
        chatMessage.setFileType(fileType);
        chatMessage.setMessageType(messageType);
        MessageSendDto messageSendDto = chatMessageService.saveMessage(chatMessage, tokenUserInfoDto);
        return getSuccessResponseVO(messageSendDto);
    }

    /**
     * 上传保存媒体类型消息到服务器
     *
     * @param request
     * @param messageId
     * @param file
     * @param cover
     * @return
     */
    @RequestMapping("/uploadFile")
    @GlobalInterceptor
    public ResponseVO uploadFile(HttpServletRequest request, @NotNull Long messageId,
                                 @NotNull MultipartFile file,
                                 @NotNull MultipartFile cover) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo(request);
        chatMessageService.saveMessageFile(userInfoDto.getUserId(), messageId, file, cover);
        return getSuccessResponseVO(null);
    }

    /**
     * 下载保存媒体文件到客户端
     *
     * @param request
     * @param response
     * @param fileId
     * @param showCover
     * @return
     */
    @RequestMapping("/downloadFile")
    @GlobalInterceptor
    public void downloadFile(HttpServletRequest request, HttpServletResponse response,
                             @NotEmpty String fileId,
                             @NotNull Boolean showCover) {
        TokenUserInfoDto userInfoDto = getTokenUserInfo(request);
        OutputStream out = null;
        FileInputStream in = null;

        try {
            File file = null;
            if (!StringTools.isNumber(fileId)) {
                String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
                String avatarPath = appConfig.getProjectFolder() + avatarFolderName + fileId + Constants.IMAGE_SUFFIX;
                if (showCover) {
                    avatarPath = avatarPath + Constants.COVER_IMAGE_SUFFIX;
                }
                file = new File(avatarPath);
                if (!file.exists()) {
                    throw new BusinessException(ResponseCodeEnum.CODE_600);
                }
            } else {
                file = chatMessageService.downloadFile(userInfoDto, Long.parseLong(fileId), showCover);
            }

            response.setContentType("application/x-msdownload;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;");
            response.setContentLengthLong(file.length());

            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int length;
            while ((length = in.read(byteData)) != -1) {
                out.write(byteData, 0, length);
            }
            out.flush();
        } catch (Exception e) {
            logger.error("下载文件失败", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    logger.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    logger.error("IO异常", e);
                }
            }
        }
    }

}
