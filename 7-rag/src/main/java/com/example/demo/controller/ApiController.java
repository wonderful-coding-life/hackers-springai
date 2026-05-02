package com.example.demo.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;

@RestController
public class ApiController {
    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private VectorStore vectorStore;

    @PostMapping("/chats")
    public String postChats(@RequestParam("message") String message) {
        var documents = vectorStore.similaritySearch(message);
        String information = String.join("\n", documents.stream().map(Document::getText).toList());
        String prompt = MessageFormat.format("""
                다음 정보를 바탕으로 질문에 답해 주세요.
                답변은 최대한 간략하게 하고, 모르는 것은 모른다고 해 주세요.
                [정보]
                {0}
                [질문]
                {1}
                """, information, message);
        return chatModel.call(prompt);
    }
}
