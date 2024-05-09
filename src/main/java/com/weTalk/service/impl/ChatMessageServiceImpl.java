package com.weTalk.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import com.weTalk.config.AppConfig;
import com.weTalk.dto.MessageSendDto;
import com.weTalk.dto.SysSettingDto;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.*;
import com.weTalk.entity.po.ChatSession;
import com.weTalk.entity.po.ChatSessionUser;
import com.weTalk.entity.po.UserContact;
import com.weTalk.entity.query.*;
import com.weTalk.exception.BusinessException;
import com.weTalk.mappers.ChatSessionMapper;
import com.weTalk.mappers.ChatSessionUserMapper;
import com.weTalk.mappers.UserContactMapper;
import com.weTalk.redis.RedisComponent;
import com.weTalk.utils.CopyTools;
import com.weTalk.utils.DateUtil;
import com.weTalk.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.weTalk.entity.po.ChatMessage;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.mappers.ChatMessageMapper;
import com.weTalk.service.ChatMessageService;
import com.weTalk.utils.StringTools;
import org.springframework.web.multipart.MultipartFile;


/**
 * 聊天消息表 业务接口实现
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

    public static final Logger logger = LoggerFactory.getLogger(ChatMessageServiceImpl.class);

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;

    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private AppConfig appConfig;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ChatMessage> findListByParam(ChatMessageQuery param) {
        return this.chatMessageMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ChatMessageQuery param) {
        return this.chatMessageMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ChatMessage> list = this.findListByParam(param);
        PaginationResultVO<ChatMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ChatMessage bean) {
        return this.chatMessageMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatMessageMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(ChatMessage bean, ChatMessageQuery param) {
        StringTools.checkParam(param);
        return this.chatMessageMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(ChatMessageQuery param) {
        StringTools.checkParam(param);
        return this.chatMessageMapper.deleteByParam(param);
    }

    /**
     * 根据MessageId获取对象
     */
    @Override
    public ChatMessage getChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId修改
     */
    @Override
    public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
        return this.chatMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    @Override
    public Integer deleteChatMessageByMessageId(Long messageId) {
        return this.chatMessageMapper.deleteByMessageId(messageId);
    }

    /**
     * 用户通过http协议发送消息给服务端后
     * 保存消息到数据库
     *
     * @param chatMessage
     * @param tokenUserInfoDto
     * @return
     */
    @Override
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
        //不是机器人的消息，先判断好友状态
        if (!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())) {
            //获取用户的所有好友
            List<String> contactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
            //如果好友列表里没有接收方好友，则无法发送消息
            if (!contactList.contains(chatMessage.getContactId())) {
                UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
                if (UserContactTypeEnum.USER == userContactTypeEnum) {
                    throw new BusinessException(ResponseCodeEnum.CODE_902);
                } else {
                    throw new BusinessException(ResponseCodeEnum.CODE_903);
                }
            }
        }

        //发送人和接收方的ID
        String sendUserId = tokenUserInfoDto.getUserId();
        String receiveId = chatMessage.getContactId();

        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(receiveId);
        String sessionId = null;
        //该消息是发送给好友 还是 发送给群组，对应着不同的sessionId
        if (UserContactTypeEnum.USER == contactTypeEnum) {
            sessionId = StringTools.createChatSessionId4User(new String[]{sendUserId, receiveId});
        } else {
            sessionId = StringTools.createChatSessionId4Group(receiveId);
        }
        chatMessage.setSessionId(sessionId);

        Long curTime = System.currentTimeMillis();
        chatMessage.setSendTime(curTime);

        //只能发送普通文本类型（CHAT）和媒体文件类型（MEDIA_CHAT）的消息
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        if (null == messageTypeEnum || !ArrayUtils.contains(new Integer[]{MessageTypeEnum.CHAT.getType(), MessageTypeEnum.MEDIA_CHAT.getType()}, chatMessage.getMessageType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //如果发送的是媒体文件类型（MEDIA_CHAT）的消息，则将消息发送状态设为发送中（SENDING），否则设为已发送（SENDED）
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum ? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();
        chatMessage.setStatus(status);

        //将用户发送来的消息处理，过滤一些Html标签，防止注入
        String messageContent = StringTools.cleanHtmlTag(chatMessage.getMessageContent());
        chatMessage.setMessageContent(messageContent);

        //更新会话信息 更新chat_session表里的会话信息
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(messageContent);
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            chatSession.setLastMessage(tokenUserInfoDto.getNickName() + ":" + messageContent);
        }
        chatSession.setLastReceiveTime(curTime);
        chatSessionMapper.updateBySessionId(chatSession, sessionId);

        //记录消息表 将消息存入chat_message表
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
        chatMessage.setContactType(contactTypeEnum.getType());
        chatMessageMapper.insert(chatMessage);

        //发送消息
        MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
        if (Constants.ROBOT_UID.equals(receiveId)) {
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            TokenUserInfoDto robot = new TokenUserInfoDto();
            robot.setUserId(sysSettingDto.getRobotUid());
            robot.setNickName(sysSettingDto.getRobotNickName());
            ChatMessage robotChatMessage = new ChatMessage();
            robotChatMessage.setContactId(sendUserId);

            //TODO 这里可以对接ChatGPT，实现AI聊天
            robotChatMessage.setMessageContent("先秦淑女步 步步有态度");
            robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
            /**
             * 当给机器人发送消息时，if程序段走完后又自调用了saveMessage方法
             * 自调用了saveMessage方法时，contactId变成了用户ID
             * 此时if程序段不符合条件，执行else程序段，给用户发送消息
             */
            saveMessage(robotChatMessage, robot);
        } else {
            messageHandler.sendMessage(messageSendDto);
        }

        return messageSendDto;
    }

    /**
     * 上传保存媒体类型消息到服务器
     *
     * @param userId
     * @param messageId
     * @param file
     * @param cover
     */
    @Override
    public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
        ChatMessage chatMessage = chatMessageMapper.selectByMessageId(messageId);
        if (null == chatMessage) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!chatMessage.getSendUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        String fileSuffix = StringTools.getFileSuffix(file.getOriginalFilename());
        //当文件大小超过系统设定时 参数错误
        if (!StringTools.isEmpty(fileSuffix)
                && ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > sysSettingDto.getMaxImageSize() * Constants.FILE_SIZE_MB) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        } else if (!StringTools.isEmpty(fileSuffix)
                && ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > sysSettingDto.getMaxVideoSize() * Constants.FILE_SIZE_MB) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        } else if (!StringTools.isEmpty(fileSuffix)
                && ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase())
                && ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase())
                && file.getSize() > sysSettingDto.getMaxFileSize() * Constants.FILE_SIZE_MB) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //文件名 最后存在服务器上的文件名就是fileRealName
        String fileName = file.getOriginalFilename();
        String fileExtendName = StringTools.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtendName;

        String month = DateUtil.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File uploadFile = new File(folder.getPath() + "/" + fileRealName);
        try {
            file.transferTo(uploadFile);
            cover.transferTo(new File(uploadFile.getPath() + Constants.COVER_IMAGE_SUFFIX));
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }

        //更新媒体消息的状态--已发送
        ChatMessage uploadInfo = new ChatMessage();
        uploadInfo.setStatus(MessageStatusEnum.SENDED.getStatus());
        ChatMessageQuery messageQuery = new ChatMessageQuery();
        messageQuery.setMessageId(messageId);
        //设置一个老状态，相当于版本号，实现乐观锁，该状态只能从0改为1（SENDING-->SENDED）
        messageQuery.setStatus(MessageStatusEnum.SENDING.getStatus());
        chatMessageMapper.updateByParam(uploadInfo, messageQuery);

        //发送消息
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setStatus(MessageStatusEnum.SENDED.getStatus());
        messageSendDto.setMessageId(messageId);
        messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSendDto.setContactId(chatMessage.getContactId());
        messageHandler.sendMessage(messageSendDto);
    }

    /**
     * 下载文件
     *
     * @param userInfoDto
     * @param messageId
     * @param showCover
     * @return
     */
    @Override
    public File downloadFile(TokenUserInfoDto userInfoDto, Long messageId, Boolean showCover) {
        ChatMessage message = chatMessageMapper.selectByMessageId(messageId);
        String contactId = message.getContactId();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        //该媒体文件消息不是发送给当前用户 或 该媒体文件消息不是当前用户自己发送的
        //即 用户可以在单聊中下载自己发送的文件也可以下载别人发送给自己的文件
        if (UserContactTypeEnum.USER == contactTypeEnum && !userInfoDto.getUserId().equals(message.getContactId()) && !userInfoDto.getUserId().equals(message.getSendUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //当前用户不在该群聊中，无法下载该群聊的文件
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setUserId(userInfoDto.getUserId());
            userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer contactCount = userContactMapper.selectCount(userContactQuery);
            if (contactCount == 0) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        String month = DateUtil.format(new Date(message.getSendTime()), DateTimePatternEnum.YYYYMM.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + month);
        if (!folder.exists()) {
            logger.info("文件不存在{}", messageId);
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
        String fileName = message.getFileName();
        String fileExtendName = StringTools.getFileSuffix(fileName);
        String fileRealName = messageId + fileExtendName;
        if (showCover != null && showCover) {
            fileRealName = fileRealName + Constants.COVER_IMAGE_SUFFIX;
        }
        File file = new File(folder.getPath() + "/" + fileRealName);
        if (!file.exists()) {
            logger.info("文件不存在{}", messageId);
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }

        return file;
    }
}