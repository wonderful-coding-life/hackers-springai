package com.example.demo.controller;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@RestController
public class ApiController {
    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    private static final String systemMessage = """
            당신은 해커스 쇼핑몰의 고객지원 상담사야.
            답변은 짥고 명료하게 해 줘.
            """;

    @PostMapping("/chats")
    public String postChats(@RequestBody String message) {
        String username = "user2";

        List<Message> messages = List.of(
                new UserMessage(message),
                new SystemMessage(systemMessage)
        );

        ToolCallback[] mcpTools = toolCallbackProvider.getToolCallbacks();

        ChatOptions chatOptions = OpenAiChatOptions.builder()
                .toolCallbacks(mcpTools)
                .toolContext(Map.of(
                        "username", username
                ))
                .build();

        Prompt prompt = new Prompt(messages, chatOptions);

        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getText();
    }
}
