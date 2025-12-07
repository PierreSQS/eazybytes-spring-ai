package com.eazybytes.mcpserverstdio.config;

import com.eazybytes.mcpserverstdio.tool.HelpDeskTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpServerConfig {

    @Bean
    List<ToolCallback> toolCallbacks(HelpDeskTools helpDeskTools) {
        // Note to Sec7_Chap82: Registering HelpDeskTools as ToolCallbacks
        return List.of(ToolCallbacks.from(helpDeskTools));
    }
}
