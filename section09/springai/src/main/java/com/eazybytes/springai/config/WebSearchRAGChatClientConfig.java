package com.eazybytes.springai.config;

import com.eazybytes.springai.advisors.TokenUsageAuditAdvisor;
import com.eazybytes.springai.rag.WebSearchDocumentRetriever;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Spring configuration class that provides a pre-configured ChatClient bean named
 * "webSearchRAGChatClient".
 * <p>
 * The created ChatClient is composed with multiple advisors:
 * - SimpleLoggerAdvisor: logs chat activity
 * - TokenUsageAuditAdvisor: audits token usage
 * - MessageChatMemoryAdvisor: attaches chat memory persistence
 * - RetrievalAugmentationAdvisor: performs retrieval-augmented generation using
 *   WebSearchDocumentRetriever (backed by the injected RestClient.Builder)
 * <p>
 * Required constructor-injected dependencies:
 * - ChatClient.Builder
 * - ChatMemory
 * - RestClient.Builder
 */
// NEW in Sec5_Chap61
@Configuration
public class WebSearchRAGChatClientConfig {

    @Bean("webSearchRAGChatClient")
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory, RestClient.Builder restClientBuilder) {
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor tokenUsageAdvisor = new TokenUsageAuditAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        // RetrievalAugmentationAdvisor using WebSearchDocumentRetriever
        var webSearchRAGAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(WebSearchDocumentRetriever.builder()
                        // configure the restClient to the Web Database
                        // limits the number of documents/results the retriever
                        // will return per query to 5
                        .restClientBuilder(restClientBuilder).maxResults(5).build())
                .build();

        return chatClientBuilder
                .defaultAdvisors(List.of(loggerAdvisor, memoryAdvisor, tokenUsageAdvisor,
                        webSearchRAGAdvisor))
                .build();
    }
}
