package com.weTalk.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.weTalk.dto.MessageSendDto;
import com.weTalk.dto.SysSettingDto;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.*;
import com.weTalk.entity.po.ChatSession;
import com.weTalk.entity.po.ChatSessionUser;
import com.weTalk.entity.query.ChatSessionQuery;
import com.weTalk.entity.query.ChatSessionUserQuery;
import com.weTalk.exception.BusinessException;
import com.weTalk.mappers.ChatSessionMapper;
import com.weTalk.mappers.ChatSessionUserMapper;
import com.weTalk.redis.RedisComponent;
import com.weTalk.utils.CopyTools;
import com.weTalk.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import com.weTalk.entity.query.ChatMessageQuery;
import com.weTalk.entity.po.ChatMessage;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.entity.query.SimplePage;
import com.weTalk.mappers.ChatMessageMapper;
import com.weTalk.service.ChatMessageService;
import com.weTalk.utils.StringTools;


/**
 * 聊天消息表 业务接口实现
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;

    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

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
            List<String> contactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
            if (!contactList.contains(chatMessage.getContactId())) {
                UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
                if (UserContactTypeEnum.USER == userContactTypeEnum) {
                    throw new BusinessException(ResponseCodeEnum.CODE_902);
                } else {
                    throw new BusinessException(ResponseCodeEnum.CODE_903);
                }
            }
        }

        String sessionId = null;
        String sendUserId = tokenUserInfoDto.getUserId();
        String contactId = chatMessage.getContactId();
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);

        if (UserContactTypeEnum.USER == contactTypeEnum) {
            sessionId = StringTools.createChatSessionId4User(new String[]{sendUserId, contactId});
        } else {
            sessionId = StringTools.createChatSessionId4Group(contactId);
        }
        chatMessage.setSessionId(sessionId);

        Long curTime = System.currentTimeMillis();
        chatMessage.setSendTime(curTime);


        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        if (null == messageTypeEnum || !ArrayUtils.contains(new Integer[]{MessageTypeEnum.CHAT.getType(), MessageTypeEnum.MEDIA_CHAT.getType()}, chatMessage.getMessageType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        Integer status = MessageTypeEnum.MEDIA_CHAT == messageTypeEnum ? MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();
        chatMessage.setStatus(status);

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
        if (Constants.ROBOT_UID.equals(contactId)) {
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
}