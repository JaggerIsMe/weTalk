package com.weTalk.ai.config;

import com.weTalk.config.AppConfig;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author Jagger
 * * @date 2025/6/25
 */
@Configuration("webSearchConfig")
public class WebSearchConfig {

    @Resource
    private AppConfig appConfig;

    @Bean
    public SearchApiWebSearchEngine searchApiWebSearchEngine(){
        return SearchApiWebSearchEngine.builder()
                .engine(appConfig.getEngine())
                .apiKey(appConfig.getApiKey()).build();
    }

}
