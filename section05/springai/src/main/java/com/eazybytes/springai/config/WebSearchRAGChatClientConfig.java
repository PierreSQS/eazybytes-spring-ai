package com.eazybytes.springai.config;

import com.eazybytes.springai.advisors.TokenUsageAuditAdvisor;
import com.eazybytes.springai.rag.WebSearchDocumentRetriever;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
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
 * The RetrievalAugmentationAdvisor is enhanced with a TranslationQueryTransformer
 * that translates user queries to English before retrieval. This ensures optimal
 * results when the embedding model is trained on English text.
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

        // TranslationQueryTransformer: translates the user query to English before retrieval.
        // This is useful when the embedding model is trained on English text and the user
        // may submit queries in other languages. If the query is already in English (or the
        // language is unknown), it is returned unchanged.
        QueryTransformer translationQueryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .targetLanguage("english")
                .build();

        // RetrievalAugmentationAdvisor using WebSearchDocumentRetriever
        // and the TranslationQueryTransformer to normalize queries to English
        var webSearchRAGAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(translationQueryTransformer)
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
