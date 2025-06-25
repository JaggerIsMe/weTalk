package com.weTalk.dto;

import com.weTalk.entity.po.ChatMessage;
import com.weTalk.entity.po.ChatSessionUser;

import java.util.List;

public class WsInitData {

    //用户会话列表
    private List<ChatSessionUser> chatSessionList;

    //用户会话消息列表
    private List<ChatMessage> chatMessageList;

    //用户新好友申请通知数
    private Integer applyCount;

    public List<ChatSessionUser> getChatSessionList() {
        return chatSessionList;
    }

    public void setChatSessionList(List<ChatSessionUser> chatSessionList) {
        this.chatSessionList = chatSessionList;
    }

    public List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public Integer getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(Integer applyCount) {
        this.applyCount = applyCount;
    }
}
