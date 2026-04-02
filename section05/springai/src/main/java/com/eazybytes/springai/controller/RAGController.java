package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/rag")
public class RAGController {

    @Value("classpath:/promptTemplates/systemPromptRandomDataTemplate.st")
    Resource promptTemplate;

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource hrSystemTemplate;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RAGController(@Qualifier("chatMemoryChatClient") ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    // Since we deactivated the RandomLoader in Sec5_Chap58, now this Handler
    // will handle the HR Policies Data, which has been loaded in the Vector Store!!!!
    @GetMapping("/random/chat")
    public ResponseEntity<String> randomChat(@RequestHeader("username") String username,
                                     @RequestParam("message") String message) {
        String response = chatClient
                .prompt()
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, username))
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .system(promptTemplate)
                .user(message)
                .call().content();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/document/chat")
    public ResponseEntity<String> documentChat(@RequestHeader("username") String username,
                                               @RequestParam("message") String message) {

        // Configure the search request used later to query the vector store
        SearchRequest searchRequest = SearchRequest.builder()
                .query(message)           // text to match against vectors
                .topK(3)                  // return up to 3 nearest neighbors
                .similarityThreshold(0.5) // ignore results below this score
                .build();

        // hold the nearest docs from the vector store
        List<Document> similarDocs =  vectorStore.similaritySearch(searchRequest);

        // collect the text of the nearest documents returned by the vector store
        // and join them into a single string.
        String similarContext = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        String answer = chatClient.prompt()
                .system(promptSystemSpec -> promptSystemSpec.text(hrSystemTemplate)
                        // the similar documents are then inserted
                        // into the prompt sent to the chat modell
                        .param("documents", similarContext))
                .advisors(a -> a.param(CONVERSATION_ID, username))
                .user(message)
                .call().content();
        return ResponseEntity.ok(answer);
    }

}