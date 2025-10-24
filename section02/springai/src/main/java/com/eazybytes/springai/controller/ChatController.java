package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
       this.chatClient = chatClient;
    }


    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {
        return chatClient
                .prompt()
                //.advisors(new TokenUsageAuditAdvisor())
                .system("""
                        You are an internal IT helpdesk assistant. Your role is to assist\s
                        employees with IT-related issues such as resetting passwords,\s
                        unlocking accounts, and answering questions related to IT policies.
                        If a user requests help with anything outside of these\s
                        responsibilities, respond politely and inform them that you are\s
                        only able to assist with IT support tasks within your defined scope.
                       \s""")
                .user(message)
                .call().content();
    }

}
