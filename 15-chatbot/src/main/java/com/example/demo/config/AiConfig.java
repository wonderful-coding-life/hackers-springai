package com.example.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(ChatOptions.builder().model("gpt-5.4-mini"))
                .build();
    }
}
