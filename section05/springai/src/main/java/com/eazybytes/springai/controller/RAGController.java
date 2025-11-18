package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api/rag")
public class RAGController {

    private final ChatClient chatClient;

    @Value("classpath:/promptTemplates/systemPromptRandomDataTemplate.st")
    Resource randomDataPromptTemplate;

    @Value("classpath:/promptTemplates/systemPromptHRTemplate.st")
    Resource hrPromptTemplate;


    public RAGController(@Qualifier("chatMemoryChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
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
                .system(promptSystemSpec -> promptSystemSpec.text(hrPromptTemplate))
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .user(message + "\n\n###### Relevant Information: ######\n")
                .call()
                .content();

        return ResponseEntity.ok(responseContent);
    }

}
