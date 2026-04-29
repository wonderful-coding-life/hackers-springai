package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
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

@RestController
@Slf4j
public class ApiController {
    @Autowired
    private OpenAiChatModel chatModel;

    @PostMapping("/meetings/summaries")
    public String postMeetingSummary(@RequestParam("file") MultipartFile file) throws IOException {
        var resource = file.getResource();
        var mimeType = MimeTypeUtils.parseMimeType("audio/mp3");
        var userMessage = UserMessage.builder()
                .text("회의 내용을 한국어로 요약해 줘.")
                .media(new Media(mimeType, resource))
                .build();

        return chatModel.call(userMessage);
    }
}
