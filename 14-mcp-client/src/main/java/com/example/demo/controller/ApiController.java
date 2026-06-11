package com.example.demo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ApiController {
    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    private static final String systemMessage = """
            상품 주문과 관련한 문의에 대해서는
            해커스 쇼핑몰의 고객지원 상담사로서 답변은 짥고 명료하게 해 주세요.
            --------
            파일 생성, 수정, 읽기 작업 시 사용자가 경로를 명시하지 않으면
            항상 다음 디렉토리를 기본 작업 디렉토리로 사용하세요.
            기본 디렉토리: D:/hackers/workspace
            --------
            사용자가 페이지 생성/수정/삭제를 요청하면 Notion MCP Tool을 사용하여 작업하세요.
            필수 정보가 충분하면 추가 확인 질문 없이 바로 실행하세요.
            새 페이지 생성 시 사용자가 위치를 지정하지 않으면 항상 다음 parent page_id 아래에 생성하세요.
            parent page_id: 37cfe2f34ce980b5910fd4593bd54c2f
            """;

    @PostMapping("/chats")
    public String postChats(@RequestBody String message) {
        return chatClient.prompt()
                .system(systemMessage)
                .user(message)
                .tools(toolCallbackProvider)
                .toolContext(Map.of("username", "user"))
                .call().content();
    }

    @Autowired
    private ToolCallingManager toolCallingManager;

    //@PostMapping("/chats")
    public String postChatsChatModel(@RequestBody String message) {
        List<Message> messages = List.of(new SystemMessage(systemMessage), new UserMessage(message));
        var options = OpenAiChatOptions.builder()
                .toolCallbacks(toolCallbackProvider.getToolCallbacks())
                .toolContext("username", "user")
                .build();
        var prompt = Prompt.builder()
                .messages(messages)
                .chatOptions(options).build();

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
