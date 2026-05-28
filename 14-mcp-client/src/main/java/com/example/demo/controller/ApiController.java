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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.List;

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

        ToolCallback[] mcpTools = toolCallbackProvider.getToolCallbacks();

        ChatOptions chatOptions = OpenAiChatOptions.builder()
                .toolCallbacks(mcpTools)
                .build();
        Prompt prompt = new Prompt(messages, chatOptions);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    private static final String notionSystemMessage = """
        당신은 Notion 문서 작성 도우미입니다.
        사용자가 페이지 생성/수정/삭제를 요청하면 Notion MCP Tool을 사용하여 작업하세요.
        필수 정보가 충분하면 추가 확인 질문 없이 바로 실행하세요.
        새 페이지 생성 시 사용자가 위치를 지정하지 않으면 항상 다음 parent page_id 아래에 생성하세요.
        parent page_id: 36e50b450ffa8012b882cbaf0aaa1f26
        """;

    @PostMapping("/notions")
    public String postNotions(@RequestParam("message") String message) {
        List<Message> messages = List.of(
                new UserMessage(message),
                new SystemMessage(notionSystemMessage)
        );

        ToolCallback[] mcpTools = toolCallbackProvider.getToolCallbacks();

        ChatOptions chatOptions = OpenAiChatOptions.builder()
                .toolCallbacks(mcpTools)
                .build();
        Prompt prompt = new Prompt(messages, chatOptions);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
