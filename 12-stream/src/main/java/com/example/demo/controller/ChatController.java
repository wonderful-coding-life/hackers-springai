package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final OpenAiChatModel chatModel;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getChatResponse(@RequestParam("message") String message) {

        //return chatModel.stream(message);

        log.info("1. request start");

        Flux<String> stream = chatModel.stream(message)
                .doOnSubscribe(s -> log.info("3. subscribed"))
                .doOnNext(token -> log.info("4. token: {}", token))
                .doOnComplete(() -> log.info("5. stream complete"));

        log.info("2. controller return");

        return stream;
    }
}
