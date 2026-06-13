package com.example.demo.advisor;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.core.Ordered;
import reactor.core.publisher.Flux;

@Slf4j
public class ExecutionTimeAdvisor implements CallAdvisor, StreamAdvisor {
    @Override
    public String getName() {
        return "execution-time-advisor";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 200;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        log.info("adviseStream request: {}", chatClientRequest.prompt().getContents());
        long start = System.currentTimeMillis();
        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);
        long elapsed =System.currentTimeMillis() - start;
        log.info("elapsed={}ms", elapsed);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        log.info("adviseStream request: {}", chatClientRequest.prompt().getContents());
        long start = System.currentTimeMillis();
        Flux<ChatClientResponse> responseFlux = streamAdvisorChain.nextStream(chatClientRequest);
        return new ChatClientMessageAggregator().aggregateChatClientResponse(responseFlux, response -> {
            long elapsed =System.currentTimeMillis() - start;
            log.info("elapsed={}ms", elapsed);
        });
    }
}
