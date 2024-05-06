package com.weTalk.websocket;

import com.weTalk.dto.MessageSendDto;
import com.weTalk.dto.WsInitData;
import com.weTalk.entity.constants.Constants;
import com.weTalk.entity.enums.MessageTypeEnum;
import com.weTalk.entity.enums.UserContactApplyStatusEnum;
import com.weTalk.entity.enums.UserContcatTypeEnum;
import com.weTalk.entity.po.ChatMessage;
import com.weTalk.entity.po.ChatSessionUser;
import com.weTalk.entity.po.UserContactApply;
import com.weTalk.entity.po.UserInfo;
import com.weTalk.entity.query.ChatMessageQuery;
import com.weTalk.entity.query.ChatSessionUserQuery;
import com.weTalk.entity.query.UserContactApplyQuery;
import com.weTalk.entity.query.UserInfoQuery;
import com.weTalk.mappers.ChatMessageMapper;
import com.weTalk.mappers.UserContactApplyMapper;
import com.weTalk.mappers.UserInfoMapper;
import com.weTalk.redis.RedisComponent;
import com.weTalk.service.ChatSessionUserService;
import com.weTalk.utils.JsonUtils;
import com.weTalk.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ChannelContextUtils {

    public static final Logger logger = LoggerFactory.getLogger(ChannelContextUtils.class);

    /**
     * Channel无法被序列化
     * 所以无法直接序列化成字节数组存储在Redis中
     * 所以存储在Map中
     */
    public static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;

    @Resource
    private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;

    /**
     * 将userId和channelId关联绑定起来
     *
     * @param userId
     * @param channel
     */
    public void addContext(String userId, Channel channel) {
        String channelId = channel.id().toString();
        logger.info("channelId:{}", channelId);
        AttributeKey attributeKey = null;
        if (!AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);

        List<String> contactIdList = redisComponent.getUserContactList(userId);
        //添加群组列表
        for (String groupId : contactIdList) {
            if (groupId.startsWith(UserContcatTypeEnum.GROUP.getPrefix())) {
                add2Group(groupId, channel);
            }
        }

        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveUserHeartBeat(userId);

        //更新用户最后连接时间
        UserInfo updateInfo = new UserInfo();
        updateInfo.setLastLoginTime(new Date());
        userInfoMapper.updateByUserId(updateInfo, userId);

        //给用户发送消息 如果用户太久没登录只发送三天前没接收到的消息
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        Long sourceLastOffTime = userInfo.getLastOffTime();
        Long lastOffTime = sourceLastOffTime;
        if (null != sourceLastOffTime && System.currentTimeMillis() - Constants.MILLI_SECOND_3DAYS_AGO > sourceLastOffTime) {
            lastOffTime = Constants.MILLI_SECOND_3DAYS_AGO;
        }

        /**
         * 1、查询会话信息 查询用户所有的会话信息 保证换了设备登录后会话同步
         */
        ChatSessionUserQuery chatSessionUserQuery = new ChatSessionUserQuery();
        chatSessionUserQuery.setUserId(userId);
        chatSessionUserQuery.setOrderBy("last_receive_time desc");
        List<ChatSessionUser> chatSessionUserList = chatSessionUserService.findListByParam(chatSessionUserQuery);
        WsInitData wsInitData = new WsInitData();
        wsInitData.setChatSessionList(chatSessionUserList);

        /**
         * 2、查询会话聊天消息
         */
        //查询所有给我发消息的联系人，获取会话消息
        //只需要在chat_message表里查询contact_id是我的UID和我加入的群聊的ID的消息
        //我加入的群聊ID
        List<String> groupIdList = contactIdList.stream().filter(item -> item.startsWith(UserContcatTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        //加上我自己的ID 因为别人给我发消息，消息的contact_id就是我的UID
        groupIdList.add(userId);
        ChatMessageQuery messageQuery = new ChatMessageQuery();
        messageQuery.setContactIdList(groupIdList);
        messageQuery.setLastReceiveTime(lastOffTime);
        List<ChatMessage> chatMessageList = chatMessageMapper.selectList(messageQuery);
        wsInitData.setChatMessageList(chatMessageList);

        /**
         * 3、查询新好友申请数
         */
        UserContactApplyQuery applyQuery = new UserContactApplyQuery();
        applyQuery.setReceiveUserId(userId);
        applyQuery.setLastApplyTimestamp(lastOffTime);
        applyQuery.setStatus(UserContactApplyStatusEnum.WAIT.getStatus());
        Integer applyCount = userContactApplyMapper.selectCount(applyQuery);
        wsInitData.setApplyCount(applyCount);

        //发送消息
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);
        messageSendDto.setExtendData(wsInitData);
        sendMsg(messageSendDto, userId);

    }

    /**
     * 加入群组会话
     *
     * @param groupId
     * @param channel
     */
    private void add2Group(String groupId, Channel channel) {
        ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);
        if (null == group) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, group);
        }
        if (null == channel) {
            return;
        }
        group.add(channel);
    }

    /**
     * 用户WebSocket连接断开后，把channel移除
     *
     * @param channel
     */
    public void removeContext(Channel channel) {
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        if (StringTools.isEmpty(userId)) {
            USER_CONTEXT_MAP.remove(userId);
        }
        //用户离线 移除心跳记录
        redisComponent.removeUserHeartBeat(userId);
        //更新用户最后离线时间
        UserInfo userInfo = new UserInfo();
        userInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.updateByUserId(userInfo, userId);
    }

    /**
     * 发送消息
     *
     * @param messageSendDto
     */
    public void sendMessage(MessageSendDto messageSendDto) {
        UserContcatTypeEnum contcatTypeEnum = UserContcatTypeEnum.getByPrefix(messageSendDto.getContactId());
        switch (contcatTypeEnum) {
            case USER:
                send2User(messageSendDto);
                break;
            case GROUP:
                send2Group(messageSendDto);
                break;
        }
    }

    /**
     * 单聊消息 发送给用户
     *
     * @param messageSendDto
     */
    public void send2User(MessageSendDto messageSendDto) {
        String contactId = messageSendDto.getContactId();
        if (StringTools.isEmpty(contactId)) {
            return;
        }
        sendMsg(messageSendDto, contactId);
        //强制下线
        if (MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDto.getMessageType())) {
            //关闭通道
            closeChannel(contactId);
        }
    }

    /**
     * 群聊消息 发送给群组
     *
     * @param messageSendDto
     */
    public void send2Group(MessageSendDto messageSendDto) {
        if (StringTools.isEmpty(messageSendDto.getContactId())) {
            return;
        }
        ChannelGroup channelGroup = GROUP_CONTEXT_MAP.get(messageSendDto.getContactId());
        if (null == channelGroup) {
            return;
        }
        channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));
    }

    /**
     * 发送单聊消息
     *
     * @param messageSendDto
     * @param receiveId
     */
    public void sendMsg(MessageSendDto messageSendDto, String receiveId) {
        Channel userChannel = USER_CONTEXT_MAP.get(receiveId);
        if (userChannel == null) {
            return;
        }

        //相对于客户端而言，联系人就是发送人，所以这里转一下再发送
        messageSendDto.setContactId(messageSendDto.getSendUserId());
        messageSendDto.setContactName(messageSendDto.getSendUserNickName());
        userChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));

    }

    /**
     * 关闭通道
     *
     * @param userId
     */
    public void closeChannel(String userId) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        redisComponent.cleanUserTokenByUserId(userId);
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (null == channel) {
            return;
        }
        channel.close();
    }

}
