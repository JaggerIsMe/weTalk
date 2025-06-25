package com.weTalk.ai.service;

import com.weTalk.dto.MessageSendDto;
import com.weTalk.dto.TokenUserInfoDto;
import com.weTalk.entity.enums.MessageTypeEnum;
import com.weTalk.entity.po.ChatMessage;
import com.weTalk.service.ChatMessageService;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Jagger
 * * @date 2025/6/25
 */
@Service("aIAsyncSendMsg")
public class AIAsyncSendMsg {

    @Resource
    private AIAssistantService aiAssistantService;

    @Lazy
    @Resource
    private ChatMessageService chatMessageService;

    @Async("aiTaskExecutor")
    public MessageSendDto asyncSendMsg(String sessionId, MessageSendDto messageSendDto, ChatMessage robotChatMessage, TokenUserInfoDto robot) {
        String aiMessage = aiAssistantService.memoryAndWebSearchHighChat(sessionId, messageSendDto.getMessageContent());
        robotChatMessage.setMessageContent(aiMessage);
//            robotChatMessage.setMessageContent("先秦淑女步 步步有态度");
        robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());

        return chatMessageService.saveMessage(robotChatMessage, robot);
    }

}
