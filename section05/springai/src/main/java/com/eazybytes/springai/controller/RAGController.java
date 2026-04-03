package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
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

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/rag")
public class RAGController {

    private final ChatClient chatClient;
    private final ChatClient webSearchChatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/promptTemplates/systemPromptRandomDataTemplate.st")
    Resource promptTemplate;

    public RAGController(@Qualifier("chatMemoryChatClient") ChatClient chatClient,
                         @Qualifier("webSearchRAGChatClient") ChatClient webSearchChatClient,
                         VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.webSearchChatClient = webSearchChatClient;
        this.vectorStore = vectorStore;
    }

    // Since we deactivated the RandomLoader in Sec5_Chap58, now this Handler
    // will handle the HR Policies Data, which has been loaded in the Vector Store!!!!
    @GetMapping("/random/chat")
    public ResponseEntity<String> randomChat(@RequestHeader("username") String username,
                                     @RequestParam("message") String message) {
        // the boilerplate code to search for similar documents
        // is now replaced by an advisor (See QuestionAdvisor.java)
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
        // the boilerplate code to search for similar documents
        // is now replaced by an advisor (See chatMemoryChatClientConfig.java)
        // also the hrSystemTemplate is no more needed since we get the HR Infos
        // from the Vector Store
        String answer = chatClient.prompt()
                .advisors(a -> a.param(CONVERSATION_ID, username))
                .user(message)
                .call().content();
        return ResponseEntity.ok(answer);
    }

    @GetMapping("/web-search/chat")
    public ResponseEntity<String> webSearchChat(@RequestHeader("username") String username,
                                                @RequestParam("message") String message) {

        // queries the Web Database in the Tavily-Service API
        String responseContent = webSearchChatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .user(message)
                .call()
                .content();

        return ResponseEntity.ok(responseContent);
    }
}