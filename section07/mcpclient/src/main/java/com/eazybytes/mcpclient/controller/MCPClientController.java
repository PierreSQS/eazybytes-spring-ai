package com.eazybytes.mcpclient.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MCPClientController {

    private final ChatClient chatClient;

    public MCPClientController(ChatClient.Builder chatClientBuilder, ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = chatClientBuilder
                // Registering the Tool Callbacks with the Chat Client
                // See McpServerConfig for Tool Callback definitions in project mcpserverstdio
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/mcp-client/chat")
    public String chatWithMCPClient(@RequestParam String message) {
        return chatClient
                .prompt()
                .user(message)
                .call()
                .content();
    }
}
