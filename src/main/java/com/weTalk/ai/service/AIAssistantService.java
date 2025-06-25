package com.weTalk.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author Jagger
 * * @date 2025/6/25
 */
@Service("aiAssistantService")
public interface AIAssistantService {

    @SystemMessage("你的名字叫WeTalk AI小助手，帮助用户解答问题和制订计划等，回复用户时用纯文本回复，字数控制在400字以内。")
    String memoryAndWebSearchHighChat(@MemoryId String memoryId, @UserMessage String message);

}
