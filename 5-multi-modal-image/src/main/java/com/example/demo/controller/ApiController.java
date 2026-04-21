package com.example.demo.controller;

import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

@RestController
public class ApiController {

    @Autowired
    private OpenAiChatModel chatModel;

    @PostMapping("/images")
    public String postImages(@RequestParam("file") MultipartFile file, @RequestParam("message") String message) throws IOException {

        var resource = new InputStreamResource(file.getInputStream());
        var mimeType = MimeTypeUtils.parseMimeType(Objects.requireNonNull(file.getContentType()));

        var userMessage = UserMessage.builder()
                .text(message)
                .media(new Media(mimeType, resource))
                .build();

        return chatModel.call(userMessage);
    }
}
