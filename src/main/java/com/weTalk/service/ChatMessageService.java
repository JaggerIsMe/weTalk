package com.weTalk.service;

import java.io.File;
import java.util.List;

import com.weTalk.dto.MessageSendDto;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.query.ChatMessageQuery;
import com.weTalk.entity.po.ChatMessage;
import com.weTalk.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;


/**
 * 聊天消息表 业务接口
 */
public interface ChatMessageService {

    /**
     * 根据条件查询列表
     */
    List<ChatMessage> findListByParam(ChatMessageQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(ChatMessageQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param);

    /**
     * 新增
     */
    Integer add(ChatMessage bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<ChatMessage> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<ChatMessage> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(ChatMessage bean, ChatMessageQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(ChatMessageQuery param);

    /**
     * 根据MessageId查询对象
     */
    ChatMessage getChatMessageByMessageId(Long messageId);


    /**
     * 根据MessageId修改
     */
    Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId);


    /**
     * 根据MessageId删除
     */
    Integer deleteChatMessageByMessageId(Long messageId);

    /**
     * 用户通过http协议发送消息给服务端后
     * 保存消息到数据库
     *
     * @param chatMessage
     * @param tokenUserInfoDto
     * @return
     */
    MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto);

    /**
     * 上传保存媒体类型消息到服务器
     *
     * @param userId
     * @param messageId
     * @param file
     * @param cover
     */
    void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover);

    /**
     * 下载文件
     *
     * @param userInfoDto
     * @param fileId
     * @param showCover
     * @return
     */
    File downloadFile(TokenUserInfoDto userInfoDto, Long fileId, Boolean showCover);

}