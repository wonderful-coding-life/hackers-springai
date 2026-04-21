package com.example.demo.controller;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class ChatBotController {
    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private ChatMemory chatMemory;

    @PostMapping("/chats")
    public String postChats(@RequestParam("id") String id, @RequestParam("message") String message) {
        if (chatMemory.get(id).isEmpty()) {
            chatMemory.add(id, new SystemMessage("정확하고 명료하게 답변 해 주세요."));
        }

        var userMessage = new UserMessage(message);
        chatMemory.add(id, userMessage);

        var prompt = new Prompt(chatMemory.get(id));
        var chatResponse = chatModel.call(prompt);

        var assistantMessage = Objects.requireNonNull(chatResponse.getResult()).getOutput();
        chatMemory.add(id, assistantMessage);

        return assistantMessage.getText();
    }
}
