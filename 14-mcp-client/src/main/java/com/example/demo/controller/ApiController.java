package com.example.demo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ApiController {
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
            parent page_id: 36ede921d30a80c89a71e8eaf81d9b57
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
}
