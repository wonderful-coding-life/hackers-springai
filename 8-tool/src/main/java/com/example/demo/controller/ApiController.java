package com.example.demo.controller;

import com.example.demo.tool.ProductOrderTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ApiController {
    @Autowired
    private OpenAiChatModel chatModel;
    @Autowired
    private ProductOrderTool productOrderTool;

    private static final String systemMessage = """
            당신은 해커스 쇼핑몰의 고객지원 상담사야.
            답변은 짥고 명료하게 해 줘.
            """;

    @PostMapping("/chats")
    public String postChats(@RequestBody String message) {
        String username = "user";

        List<Message> messages = List.of(
                new UserMessage(message),
                new SystemMessage(systemMessage)
        );

        ToolCallback[] toolCallbacks = ToolCallbacks.from(productOrderTool);

        ChatOptions chatOptions = OpenAiChatOptions.builder()
                .toolCallbacks(toolCallbacks)
                .toolContext("username", username)
                .build();
        Prompt prompt = new Prompt(messages, chatOptions);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
