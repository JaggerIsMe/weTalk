package com.weTalk.ai.config;

import com.weTalk.ai.service.AIAssistantService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.web.search.WebSearchTool;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Jagger
 * * @date 2025/6/25
 */
@Configuration("assistantConfig")
public class AssistantConfig {

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Resource
    private SearchApiWebSearchEngine searchApiWebSearchEngine;

    @Bean
    public AIAssistantService aiAssistantService(){
        return AiServices.builder(AIAssistantService.class)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .chatLanguageModel(chatLanguageModel)
                .tools(new WebSearchTool(searchApiWebSearchEngine)).build();
    }

}
