package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rag")
public class RAGController {

    public static final String CONVERSION_ID = "CONVERSION_ID";
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/promptTemplates/systemPromptRandomDataTemplate.st")
    Resource randomDataPromptTemplate;

    @Value("classpath:/promptTemplates/systemPromptHRTemplate.st")
    Resource hrPromptTemplate;


    public RAGController(@Qualifier("chatMemoryChatClient") ChatClient chatClient,
                         VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @GetMapping("/random/chat")
    public ResponseEntity<String> randomChat(@RequestHeader("username") String username,
                                             @RequestParam("message") String message) {

        SearchRequest searchRequest = SearchRequest.builder()
                .query(message)
                .topK(3)
                .similarityThreshold(0.5)
                .build();

        String similarDocuments = vectorStore.similaritySearch(searchRequest).stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        String responseContent = chatClient.prompt()
                .system(promptSystemSpec -> {
                    promptSystemSpec.text(randomDataPromptTemplate);
                    promptSystemSpec.param("documents", similarDocuments);
                })
                .advisors(advisorSpec -> advisorSpec.param(CONVERSION_ID, username))
                .user(message + "\n\nRelevant Information:\n" + similarDocuments)
                .call()
                .content();

        return ResponseEntity.ok(responseContent);
    }

    @GetMapping("/document/chat")
    public ResponseEntity<String> documentChat(@RequestHeader("username") String username,
                                             @RequestParam("message") String message) {

        SearchRequest searchRequest = SearchRequest.builder()
                .query(message)
                .topK(3)
                .similarityThreshold(0.5)
                .build();

        String similarDocuments = vectorStore.similaritySearch(searchRequest).stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        String responseContent = chatClient.prompt()
                .system(promptSystemSpec -> {
                    promptSystemSpec.text(hrPromptTemplate);
                    promptSystemSpec.param("documents", similarDocuments);
                })
                .advisors(advisorSpec -> advisorSpec.param(CONVERSION_ID, username))
                .user(message + "\n\nRelevant Information:\n" + similarDocuments)
                .call()
                .content();

        return ResponseEntity.ok(responseContent);
    }

}
