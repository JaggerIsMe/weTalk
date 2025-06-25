package com.weTalk.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.weTalk.dto.MessageSendDto;
import com.weTalk.entity.enums.MessageTypeEnum;
import com.weTalk.entity.enums.UserContactStatusEnum;
import com.weTalk.entity.enums.UserContactTypeEnum;
import com.weTalk.entity.po.UserContact;
import com.weTalk.entity.query.UserContactQuery;
import com.weTalk.mappers.UserContactMapper;
import com.weTalk.websocket.MessageHandler;
import org.springframework.stereotype.Service;

import com.weTalk.entity.enums.PageSize;
import com.weTalk.entity.query.ChatSessionUserQuery;
import com.weTalk.entity.po.ChatSessionUser;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.entity.query.SimplePage;
import com.weTalk.mappers.ChatSessionUserMapper;
import com.weTalk.service.ChatSessionUserService;
import com.weTalk.utils.StringTools;


/**
 * 会话用户表 业务接口实现
 */
@Service("chatSessionUserService")
public class ChatSessionUserServiceImpl implements ChatSessionUserService {

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private MessageHandler messageHandler;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ChatSessionUser> findListByParam(ChatSessionUserQuery param) {
        return this.chatSessionUserMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ChatSessionUserQuery param) {
        return this.chatSessionUserMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ChatSessionUser> findListByPage(ChatSessionUserQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ChatSessionUser> list = this.findListByParam(param);
        PaginationResultVO<ChatSessionUser> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ChatSessionUser bean) {
        return this.chatSessionUserMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ChatSessionUser> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatSessionUserMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ChatSessionUser> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.chatSessionUserMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(ChatSessionUser bean, ChatSessionUserQuery param) {
        StringTools.checkParam(param);
        return this.chatSessionUserMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(ChatSessionUserQuery param) {
        StringTools.checkParam(param);
        return this.chatSessionUserMapper.deleteByParam(param);
    }

    /**
     * 根据UserIdAndContactId获取对象
     */
    @Override
    public ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId) {
        return this.chatSessionUserMapper.selectByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据UserIdAndContactId修改
     */
    @Override
    public Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId) {
        return this.chatSessionUserMapper.updateByUserIdAndContactId(bean, userId, contactId);
    }

    /**
     * 根据UserIdAndContactId删除
     */
    @Override
    public Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId) {
        return this.chatSessionUserMapper.deleteByUserIdAndContactId(userId, contactId);
    }

    /**
     * 修改昵称 更新相关表（chat_session_user表）的相关冗余信息
     *
     * @param contactName
     * @param contactId
     */
    @Override
    public void updateRedundantInfo(String contactName, String contactId) {
        ChatSessionUser updateInfo = new ChatSessionUser();
        updateInfo.setContactName(contactName);
        ChatSessionUserQuery chatSessionUserQuery = new ChatSessionUserQuery();
        chatSessionUserQuery.setContactId(contactId);
        this.chatSessionUserMapper.updateByParam(updateInfo, chatSessionUserQuery);

        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (UserContactTypeEnum.GROUP == contactTypeEnum) {
            //修改群昵称发送WebSocket消息通知
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setContactType(contactTypeEnum.getType());
            messageSendDto.setContactId(contactId);
            messageSendDto.setExtendData(contactName);
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            messageHandler.sendMessage(messageSendDto);
        } else {
            //修改用户昵称
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactType(UserContactTypeEnum.USER.getType());
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            List<UserContact> userContactList = userContactMapper.selectList(userContactQuery);
            //需要给我的每一个好友的会话分别发送信息，更新会话里我的昵称
            for (UserContact userContact : userContactList) {
                MessageSendDto messageSendDto = new MessageSendDto();
                messageSendDto.setContactType(contactTypeEnum.getType());
                messageSendDto.setContactId(userContact.getUserId());
                messageSendDto.setExtendData(contactName);
                messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
                messageSendDto.setSendUserId(contactId);
                messageSendDto.setSendUserNickName(contactName);
                messageHandler.sendMessage(messageSendDto);
            }
        }
    }

}