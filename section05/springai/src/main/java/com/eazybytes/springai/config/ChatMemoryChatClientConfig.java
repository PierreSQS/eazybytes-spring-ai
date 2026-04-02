package com.eazybytes.springai.config;

import com.eazybytes.springai.advisors.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatMemoryChatClientConfig {

    @Bean
    ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder().maxMessages(15)
                .chatMemoryRepository(jdbcChatMemoryRepository).build();
    }

    // ChatClient with ChatMemory and RetrievalAugmentationAdvisor
    @Bean("chatMemoryChatClient")
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory,
                                 RetrievalAugmentationAdvisor retrievalAugmentationAdvisor) {
        // logger advisor to log the interactions between the user and the model,
        // which can be useful for debugging and monitoring purposes
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();

        // Token Usage advisor to track the number of tokens used
        // in each interaction with the model,
        Advisor tokenUsageAdvisor = new TokenUsageAuditAdvisor();

        // New to chapter (Sec5_Chap57)
        // memory advisor to manage the chat memory and
        // provide context to the model based on the conversation history
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        return chatClientBuilder
                .defaultAdvisors(List.of(loggerAdvisor, memoryAdvisor,
                        tokenUsageAdvisor, retrievalAugmentationAdvisor))
                .build();
    }

    // New to chapter (Sec5_Chap60)
    // RetrievalAugmentationAdvisor for the Bean above
    @Bean
    public RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStore vectorStore) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        // Only retrieve documents with a similarity score above 0.5
                        .similarityThreshold(0.5)
                        // Retrieve the top 3 most relevant documents based on the similarity score
                        .topK(3)
                        // The vector store from which to retrieve relevant documents
                        // based on the user's query
                        .vectorStore(vectorStore)
                        .build())
                .build();
    }
}
