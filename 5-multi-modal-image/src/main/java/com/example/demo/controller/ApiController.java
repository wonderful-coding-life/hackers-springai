package com.example.demo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
public class ApiController {

    @Autowired
    private OpenAiChatModel chatModel;

    @PostMapping("/images")
    public String postImages(@RequestParam("file") MultipartFile file, @RequestParam("message") String message) throws IOException {

        var resource = file.getResource();
        var mimeType = MimeTypeUtils.parseMimeType(file.getContentType());

        var userMessage = UserMessage.builder()
                .text(message)
                .media(new Media(mimeType, resource))
                .build();

        return chatModel.call(userMessage);
    }

    @Autowired
    private ChatClient chatClient;

    @PostMapping("/receipts")
    public String postReceipts(@RequestParam("file") List<MultipartFile> files) {
        var media = files.stream()
                .map(file -> Media.builder()
                        .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                        .data(file.getResource())
                        .build())
                .toArray(Media[]::new);

        return chatClient.prompt()
                .user(spec -> spec
                        .text("영수증의 날짜, 상호, 금액을 표 형태로 정리해 주세요.")
                        .media(media))
                .call().content();
    }
}
