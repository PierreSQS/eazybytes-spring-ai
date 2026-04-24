package com.eazybytes.springai.config;

import com.eazybytes.springai.advisors.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import com.eazybytes.springai.tools.TimeTools;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TimeChatClientConfig {


    // New to Sec6_Chap66
    @Bean("timeChatClient")
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory,
                                 TimeTools timetools) { // note the TimeTools param

        // Introduced in Sec2_Chap19
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();

        // Introduced in Sec2_Chap20
        Advisor tokenUsageAdvisor = new TokenUsageAuditAdvisor();

        // Introduced Sec2_Chap45
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return chatClientBuilder
                .defaultAdvisors(List.of(loggerAdvisor, memoryAdvisor, tokenUsageAdvisor))
                // note the defaultTools with TimeTools
                .defaultTools(timetools)
                .build();
    }
}
