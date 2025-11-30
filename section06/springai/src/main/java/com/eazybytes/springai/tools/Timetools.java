package com.eazybytes.springai.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class Timetools {

    private static final Logger logger = LoggerFactory.getLogger(Timetools.class);
    
    @Tool(name="getCurrentLocalTime", description="Get the current time in the user's timezone")
    public String getCurrentLocalTime() {
        logger.info("###  the current time in the user's timezone ###");
        return LocalTime.now().toString();
    }
}
