package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OpenAiApplication implements ApplicationRunner {
    @Autowired
    private OpenAiChatModel chatModel;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var options = OpenAiChatOptions.builder()
                .model("gpt-5.4")
                .n(1)
                .temperature(1.0)
                .topP(1.0)
                .serviceTier("default") // default, flex, priority
                .reasoningEffort("low") // low, medium, high
                .build();
        var prompt = Prompt.builder()
                .messages(new UserMessage("""
                        커피를 마시고 싶은 마음이 들게 하는 한줄 광고 문구를 만들어줘
                        """))
                .chatOptions(options)
                .build();

        var chatResponse = chatModel.call(prompt);

        for (var result : chatResponse.getResults()) {
            log.info("{}", result.getOutput().getText());
        }

        var metadata = chatResponse.getMetadata();
        log.info("model = {}", metadata.getModel());
        log.info("usage prompt = {}, completion = {}, total = {}",
                metadata.getUsage().getPromptTokens(),
                metadata.getUsage().getCompletionTokens(),
                metadata.getUsage().getTotalTokens());
        log.info("request limit = {}, remaining = {}, reset = {}",
                metadata.getRateLimit().getRequestsLimit(),
                metadata.getRateLimit().getRequestsRemaining(),
                metadata.getRateLimit().getRequestsReset());
        log.info("token limit = {}, remaining = {}, reset = {}",
                metadata.getRateLimit().getTokensLimit(),
                metadata.getRateLimit().getTokensRemaining(),
                metadata.getRateLimit().getTokensReset());
    }
}
