package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/rag")
public class RAGController {

    private final ChatClient chatClient;
    private final ChatClient webSearchChatClient;

    public RAGController(@Qualifier("chatMemoryChatClient") ChatClient chatClient,
                         @Qualifier("webSearchRAGChatClient") ChatClient webSearchChatClient) {
        this.chatClient = chatClient;
        this.webSearchChatClient = webSearchChatClient;
    }

    @GetMapping("/random/chat")
    public ResponseEntity<String> randomChat(@RequestHeader("username") String username,
                                             @RequestParam("message") String message) {

        // the boilerplate code to search for similar documents
        // is now replaced by an advisor (See chatMemoryChatClientConfig.java)
        String responseContent = chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .user(message + "\n\n### Relevant Information:###\n")
                .call()
                .content();

        return ResponseEntity.ok(responseContent);
    }

    @GetMapping("/document/chat")
    public ResponseEntity<String> documentChat(@RequestHeader("username") String username,
                                               @RequestParam("message") String message) {

        // the boilerplate code to search for similar documents
        // is now replaced by an advisor (See chatMemoryChatClientConfig.java)
        String responseContent = chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .user(message + "\n\n###### Relevant Information: ######\n")
                .call()
                .content();

        return ResponseEntity.ok(responseContent);
    }

    @GetMapping("/web-search/chat")
    public ResponseEntity<String> webSearchChat(@RequestHeader("username") String username,
                                                @RequestParam("message") String message) {

        // queries the Web Database in the Tavily-Service API
        String responseContent = webSearchChatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .user(message + "\n\n###### Relevant Information: ######\n")
                .call()
                .content();

        return ResponseEntity.ok(responseContent);
    }
}
