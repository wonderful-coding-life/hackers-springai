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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
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
    public String postChats(@RequestParam("message") String message) {
        String memberId = "user123";

        String userMessage = MessageFormat.format("""
                회원 아이디: {0}
                요청 내용: {1}
                """, memberId, message);

        List<Message> messages = List.of(
                new UserMessage(userMessage),
                new SystemMessage(systemMessage)
        );
        ChatOptions chatOptions = OpenAiChatOptions.builder()
                .toolCallbacks(ToolCallbacks.from(productOrderTool))
                .build();
        Prompt prompt = new Prompt(messages, chatOptions);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
