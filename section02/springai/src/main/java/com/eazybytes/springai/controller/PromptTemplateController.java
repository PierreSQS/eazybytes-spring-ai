package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PromptTemplateController {

    public static final String SYSTEM_PROMPT = """
            You are a professional customer service assistant which helps drafting email
            responses to improve the productivity of the customer support team
            """;
    public static final String CUSTOMER_NAME_PARAM = "customerName";
    public static final String CUSTOMER_MSG_PARAM = "customerMessage";

    private final ChatClient chatClient;

    public PromptTemplateController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Value("classpath:/promptTemplates/userPromptTemplate.st")
    Resource userPromptTemplate;

    @GetMapping("/email")
    public String emailResponse(@RequestParam(CUSTOMER_NAME_PARAM) String customerName,
            @RequestParam(CUSTOMER_MSG_PARAM) String customerMessage) {
        return chatClient
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(promptTemplateSpec ->
                        promptTemplateSpec.text(userPromptTemplate)
                                .param(CUSTOMER_NAME_PARAM, customerName)
                                .param(CUSTOMER_MSG_PARAM, customerMessage))
                .call().content();
    }

    @GetMapping("/emailV2")
    public String emailResponseV2(@RequestParam(CUSTOMER_NAME_PARAM) String customerName,
                                  @RequestParam(CUSTOMER_MSG_PARAM) String customerMessage) {

        PromptTemplate promptTemplate = new PromptTemplate(userPromptTemplate);
        Prompt prompt = promptTemplate
                .create(Map.of(CUSTOMER_NAME_PARAM, customerName, CUSTOMER_MSG_PARAM, customerMessage));

        return chatClient
                .prompt(prompt)
                .system(SYSTEM_PROMPT)
                .call()
                .content();

    }

    @GetMapping("/emailV3")
    public String emailResponseV3(@RequestParam(CUSTOMER_NAME_PARAM) String customerName,
                                  @RequestParam(CUSTOMER_MSG_PARAM) String customerMessage) {

        PromptTemplate promptTemplate = PromptTemplate.builder().resource(userPromptTemplate).build();

        Prompt prompt = promptTemplate
                .create(Map.of(CUSTOMER_NAME_PARAM, customerName, CUSTOMER_MSG_PARAM, customerMessage));

        return chatClient
                .prompt(prompt)
                .system(SYSTEM_PROMPT)
                .call()
                .content();

    }

}
