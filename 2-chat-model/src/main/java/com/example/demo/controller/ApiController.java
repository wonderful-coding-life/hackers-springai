package com.example.demo.controller;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @Autowired
    private OpenAiChatModel chatModel;

    @PostMapping("/chats")
    public String postChats(@RequestParam("message") String message) {
        return chatModel.call(message);
    }
}
