package com.weTalk.service.impl;

import com.weTalk.dto.MessageSendDto;
import com.weTalk.dto.SysSettingDto;
import com.weTalk.dto.UserContactSearchResultDto;
import com.weTalk.entity.enums.*;
import com.weTalk.entity.po.*;
import com.weTalk.entity.query.*;
import com.weTalk.entity.vo.PaginationResultVO;
import com.weTalk.exception.BusinessException;
import com.weTalk.mappers.*;
import com.weTalk.redis.RedisComponent;
import com.weTalk.service.UserContactService;
import com.weTalk.utils.CopyTools;
import com.weTalk.utils.StringTools;
import com.weTalk.websocket.ChannelContextUtils;
import com.weTalk.websocket.MessageHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 联系人 业务接口实现
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

    @Resource
    private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

    @Resource
    private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private ChannelContextUtils channelContextUtils;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserContact> findListByParam(UserContactQuery param) {
        return this.userContactMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserContactQuery param) {
        return this.userContactMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserContact> findListByPage(UserContactQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserContact> list = this.findListByParam(param);
        PaginationResultVO<UserContact> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserContact bean) {
        return this.userContactMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserContact> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserContact> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userContactMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserContact bean, UserContactQuery param) {
        StringTools.checkParam(param);
        return this.userContactMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserContactQuery param) {
        StringTools.checkParam(param);
        return this.userContactMapper.deleteByParam(param);
    }

    /**
     * 根据UserIdAndContactId获取对象
     */
    @Override
    public UserContact getUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
    }

    /**
     * 根据UserIdAndContactId修改
     */
    @Override
    public Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
        return this.userContactMapper.updateByUserIdAndContactId(bean, userId, contactId);
    }

    /**
     * 根据UserIdAndContactId删除
     */
    @Override
    public Integer deleteUserContactByUserIdAndContactId(String userId, String contactId) {
        return this.userContactMapper.deleteByUserIdAndContactId(userId, contactId);
    }

    /**
     * 搜索联系人或群
     *
     * @param userId    发起搜索的ID
     * @param contactId 被搜索的ID
     * @return
     */
    @Override
    public UserContactSearchResultDto searchContact(String userId, String contactId) {
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (null == typeEnum) {
            return null;
        }
        UserContactSearchResultDto resultDto = new UserContactSearchResultDto();
        switch (typeEnum) {
            case USER:
                UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
                if (null == userInfo) {
                    return null;
                }
                resultDto = CopyTools.copy(userInfo, UserContactSearchResultDto.class);
                break;
            case GROUP:
                GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
                if (null == groupInfo) {
                    return null;
                }
                resultDto.setNickName(groupInfo.getGroupName());
                break;
        }
        resultDto.setContactType(typeEnum.toString());
        resultDto.setContactId(contactId);

        //如果查询的是自己
        if (userId.equals(contactId)) {
            resultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return resultDto;
        }
        //查询是否是好友
        UserContact userContact = this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
        resultDto.setStatus(userContact == null ? null : userContact.getStatus());

        return resultDto;
    }

    /**
     * 添加联系人
     *
     * @param applyUserId
     * @param receiveUserId
     * @param contactId
     * @param contactType
     * @param applyInfo
     */
    @Override
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
        //加群 判断群有没有满
        if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer count = userContactMapper.selectCount(userContactQuery);
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            if (count >= sysSettingDto.getMaxGroupMemberCount()) {
                throw new BusinessException("该群成员已满，无法加入");
            }
        }
        Date curDate = new Date();
        //若同意，双方添加好友
        List<UserContact> contactList = new ArrayList<>();
        //申请人添加对方
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(contactId);
        userContact.setContactType(contactType);
        userContact.setCreateTime(curDate);
        userContact.setLastUpdateTime(curDate);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        contactList.add(userContact);
        //如果是申请好友，接受人也要添加申请人；如果是申请加群，则接受人不需要添加申请人
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            userContact = new UserContact();
            userContact.setUserId(receiveUserId);
            userContact.setContactId(applyUserId);
            userContact.setContactType(contactType);
            userContact.setCreateTime(curDate);
            userContact.setLastUpdateTime(curDate);
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            contactList.add(userContact);
        }
        //批量插入
        userContactMapper.insertOrUpdateBatch(contactList);

        //如果是好友，接受人也添加申请人为好友 添加缓存
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            redisComponent.addUserContact(receiveUserId, applyUserId);
        }
        redisComponent.addUserContact(applyUserId, contactId);

        //创建会话 发送消息
        String sessionId = null;
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            sessionId = StringTools.createChatSessionId4User(new String[]{applyUserId, contactId});
        } else {
            sessionId = StringTools.createChatSessionId4Group(contactId);
        }

        List<ChatSessionUser> chatSessionUserList = new ArrayList<>();
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            //创建会话
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(applyInfo);
            chatSession.setLastReceiveTime(curDate.getTime());
            //更新chat_session表里的消息信息 更新一条数据
            this.chatSessionMapper.insertOrUpdate(chatSession);

            //为申请人创建会话session
            ChatSessionUser applySessionUser = new ChatSessionUser();
            applySessionUser.setUserId(applyUserId);
            applySessionUser.setContactId(contactId);
            applySessionUser.setSessionId(sessionId);
            UserInfo contactUser = this.userInfoMapper.selectByUserId(contactId);
            applySessionUser.setContactName(contactUser.getNickName());
            chatSessionUserList.add(applySessionUser);

            //为接受人创建会话session
            ChatSessionUser receiveSessionUser = new ChatSessionUser();
            receiveSessionUser.setUserId(contactId);
            receiveSessionUser.setContactId(applyUserId);
            receiveSessionUser.setSessionId(sessionId);
            UserInfo applyUser = this.userInfoMapper.selectByUserId(applyUserId);
            receiveSessionUser.setContactName(applyUser.getNickName());
            chatSessionUserList.add(receiveSessionUser);
            //批量更新chat_session_user表里的用户会话信息 更新两条数据
            this.chatSessionUserMapper.insertOrUpdateBatch(chatSessionUserList);

            //记录消息表 把消息记录到chat_message表里
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            chatMessage.setMessageContent(applyInfo);
            chatMessage.setSendUserId(applyUserId);
            chatMessage.setSendUserNickName(applyUser.getNickName());
            chatMessage.setSendTime(curDate.getTime());
            chatMessage.setContactId(contactId);
            chatMessage.setContactType(UserContactTypeEnum.USER.getType());
            chatMessageMapper.insert(chatMessage);

            //开始发消息
            MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
            //发送给接受好友申请的用户 即发送给接受人
            messageHandler.sendMessage(messageSendDto);
            //发送给申请人，此时消息发送方就是接受人，联系人就是申请人（和上面反过来）
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
            messageSendDto.setContactId(applyUserId);
            messageSendDto.setExtendData(contactUser);
            messageHandler.sendMessage(messageSendDto);

        } else {
            //申请加入群组
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(applyUserId);
            chatSessionUser.setContactId(contactId);
            GroupInfo groupInfo = this.groupInfoMapper.selectByGroupId(contactId);
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setSessionId(sessionId);
            this.chatSessionUserMapper.insert(chatSessionUser);

            //新成员加群后，发送群通知消息
            UserInfo applyUserInfo = this.userInfoMapper.selectByUserId(applyUserId);
            String sendMessage = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), applyUserInfo.getNickName());
            //增加session信息
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastReceiveTime(curDate.getTime());
            chatSession.setLastMessage(sendMessage);
            this.chatSessionMapper.insertOrUpdate(chatSession);
            //增加聊天消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
            chatMessage.setMessageContent(sendMessage);
            chatMessage.setSendTime(curDate.getTime());
            chatMessage.setContactId(contactId);
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            this.chatMessageMapper.insert(chatMessage);

            //将群组添加进申请人的联系人缓存里
            redisComponent.addUserContact(applyUserId, groupInfo.getGroupId());

            //将申请人的通信通道添加进群组通道里
            channelContextUtils.addUser2Group(applyUserId, groupInfo.getGroupId());

            //发送群消息
            MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
            messageSendDto.setContactId(contactId);
            //获取群员数量
            UserContactQuery userContactQuery = new UserContactQuery();
            userContactQuery.setContactId(contactId);
            userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer memberCount = this.userContactMapper.selectCount(userContactQuery);
            messageSendDto.setMemberCount(memberCount);
            messageSendDto.setContactName(groupInfo.getGroupName());
            //发消息
            messageHandler.sendMessage(messageSendDto);
        }

    }

    /**
     * 删除或拉黑联系人
     *
     * @param userId
     * @param contactId
     * @param statusEnum
     */
    @Override
    public void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum) {
        //移除好友
        UserContact userContact = new UserContact();
        userContact.setStatus(statusEnum.getStatus());
        userContactMapper.updateByUserIdAndContactId(userContact, userId, contactId);

        //反过来 也要在好友的联系人列表中更新我的状态
        UserContact friendContact = new UserContact();
        if (UserContactStatusEnum.DEL_FRIEND == statusEnum) {
            friendContact.setStatus(UserContactStatusEnum.DEL_BY_FRIEND.getStatus());
        } else if (UserContactStatusEnum.BLACK_FRIEND == statusEnum) {
            friendContact.setStatus(UserContactStatusEnum.BLACK_BY_FRIEND.getStatus());
        }
        userContactMapper.updateByUserIdAndContactId(friendContact, contactId, userId);

        //TODO 从我的好友列表缓存中删除该好友

        //TODO 从好友的好友列表缓存中删除我

    }

    /**
     * 添加机器人好友
     *
     * @param userId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact4Robot(String userId) {
        Date curDate = new Date();
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        String contactId = sysSettingDto.getRobotUid();
        String contactName = sysSettingDto.getRobotNickName();
        String sendMessage = sysSettingDto.getRobotWelcome();
        sendMessage = StringTools.cleanHtmlTag(sendMessage);
        //增加机器人好友
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setContactId(contactId);
        userContact.setContactType(UserContactTypeEnum.USER.getType());
        userContact.setCreateTime(curDate);
        userContact.setLastUpdateTime(curDate);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        this.userContactMapper.insert(userContact);
        //增加会话信息
        String sessionId = StringTools.createChatSessionId4User(new String[]{userId, contactId});
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(sendMessage);
        chatSession.setLastReceiveTime(curDate.getTime());
        this.chatSessionMapper.insert(chatSession);
        //增加会话人信息
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setUserId(userId);
        chatSessionUser.setContactId(contactId);
        chatSessionUser.setContactName(contactName);
        chatSessionUser.setSessionId(sessionId);
        this.chatSessionUserMapper.insert(chatSessionUser);
        //增加聊天消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
        chatMessage.setMessageContent(sendMessage);
        chatMessage.setSendUserId(contactId);
        chatMessage.setSendUserNickName(contactName);
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setContactId(userId);
        chatMessage.setContactType(UserContactTypeEnum.USER.getType());
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageMapper.insert(chatMessage);
    }
}