package com.example.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> getChatResponse(@RequestParam("message") String message) {

        //return chatModel.stream(message);

        log.info("1. request start");

        // ObjectMapper.writeValueAsString()은 Java 문자열을 JSON 문자열 형태로 안전하게 직렬화하며,
        // 따옴표 추가와 escape 처리까지 자동 수행한다.
        Flux<String> stream = chatModel.stream(message)
                .map(objectMapper::writeValueAsString)
                .doOnSubscribe(s -> log.info("3. subscribed"))
                .doOnNext(token -> log.info("4. token: {}", token))
                .doOnComplete(() -> log.info("5. stream complete"));

        log.info("2. controller return");

        return stream;
    }
}
