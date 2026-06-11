package com.example.demo.controller;

import com.example.demo.tool.ProductOrderTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class ApiController {
    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ProductOrderTool productOrderTool;

    private static final String systemMessage = """
            당신은 해커스 쇼핑몰의 고객지원 상담사야.
            답변은 짥고 명료하게 해 줘.
            """;

    @PostMapping("/chats")
    public String postChats(@RequestBody String message) {
        return chatClient.prompt()
                .system(systemMessage)
                .user(message)
                .tools(productOrderTool)
                .toolContext(Map.of("username", "user"))
                .call().content();
    }

    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private ToolCallingManager toolCallingManager;

    //@PostMapping("/chats")
    public String postChatsWithChatModel(@RequestBody String message) {
        String username = "user";

        List<Message> messages = List.of(
                new SystemMessage(systemMessage),
                new UserMessage(message)
        );

        ToolCallback[] toolCallbacks = ToolCallbacks.from(productOrderTool);

        ChatOptions chatOptions = OpenAiChatOptions.builder()
                .toolCallbacks(toolCallbacks)
                .toolContext("username", username)
                .build();
        Prompt prompt = new Prompt(messages, chatOptions);

        for (int i = 0; i < 10; i++) {
            ChatResponse response = chatModel.call(prompt);
            if (!response.hasToolCalls()) {
                return response.getResult().getOutput().getText();
            }
            ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, response);
            prompt = new Prompt(result.conversationHistory(), prompt.getOptions());
        }
        throw new IllegalStateException("Too many tool calling iterations");
    }
}
