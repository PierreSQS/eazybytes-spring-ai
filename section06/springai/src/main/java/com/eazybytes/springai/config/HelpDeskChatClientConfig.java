package com.eazybytes.springai.config;

import com.eazybytes.springai.advisors.TokenUsageAuditAdvisor;
import com.eazybytes.springai.tools.TimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

// New in Sec6_Chap70
@Configuration
public class HelpDeskChatClientConfig {

    // New in Sec6_Chap71
    @Value("classpath:/promptTemplates/helpDeskSystemPromptTemplate.st")
    Resource systemPromptTemplate;

    @Bean("helpDeskChatClient")
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory, TimeTools timeTools) {

        Advisor loggerAdvisor = new SimpleLoggerAdvisor();

        Advisor tokenUsageAdvisor = new TokenUsageAuditAdvisor();

        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        return chatClientBuilder
                .defaultSystem(systemPromptTemplate)
                // default Tooling
                .defaultTools(timeTools)
                .defaultAdvisors(List.of(loggerAdvisor, memoryAdvisor,tokenUsageAdvisor))
                .build();
    }
}
