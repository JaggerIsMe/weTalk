package com.weTalk.entity.po;

import com.weTalk.entity.enums.UserContactTypeEnum;
import com.weTalk.utils.StringTools;

import java.io.Serializable;


/**
 * 会话用户表
 */
public class ChatSessionUser implements Serializable {


    private static final long serialVersionUID = -1645665985744850221L;
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 联系人ID
     */
    private String contactId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 联系人名称
     */
    private String contactName;

    /**
     * 最后接收的消息
     * <p>
     * 该属性来自 ChatSession
     * 由chat_session_user表和chat_session表连接查询获得
     */
    private String lastMessage;

    /**
     * 最后接收消息时间毫秒
     * <p>
     * 该属性来自 ChatSession
     * 由chat_session_user表和chat_session表连接查询获得
     */
    private Long lastReceiveTime;

    /**
     * 群成员数
     */
    private Integer memberCount;

    /**
     * 会话类型
     * 单聊还是群聊
     * <p>
     * 该属性不在数据库设计里
     */
    private Integer contactType;


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactId() {
        return this.contactId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactName() {
        return this.contactName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getLastReceiveTime() {
        return lastReceiveTime;
    }

    public void setLastReceiveTime(Long lastReceiveTime) {
        this.lastReceiveTime = lastReceiveTime;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getContactType() {
        if (StringTools.isEmpty(contactId)) {
            return null;
        }
        return UserContactTypeEnum.getByPrefix(contactId).getType();
    }

    public void setContactType(Integer contactType) {
        this.contactType = contactType;
    }

    @Override
    public String toString() {
        return "用户ID:" + (userId == null ? "空" : userId) + "，联系人ID:" + (contactId == null ? "空" : contactId) + "，会话ID:" + (sessionId == null ? "空" : sessionId) + "，联系人名称:" + (contactName == null ? "空" : contactName);
    }
}
