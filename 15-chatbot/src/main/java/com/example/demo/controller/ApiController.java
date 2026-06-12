package com.example.demo.controller;

import com.example.demo.advisor.ExecutionTimeAdvisor;
import com.example.demo.tool.ProductOrderTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestController
public class ApiController {
    @Autowired
    private ChatClient chatClient;
    @Autowired
    private ChatMemory chatMemory;
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private ProductOrderTool productOrderTool;

    private static final String systemMessage = """
            당신은 해커스 쇼핑몰의 고객지원 상담사야.
            정확히 알고 있는 사실에 근거해서 답변하고 모르는 것은 고객센터 02-537-5000으로 안내하고 답변은 짥고 명료해.
            답변은 순수 텍스트(Plain Text) 형식으로 작성하고, Markdown 문법은 사용 금지.
            사용자가 인사만 하거나 구체적인 문의 없이 말을 건 경우에는 간단히 인사하고 문의 내용을 요청해. 이때 문의 유형 예시는 나열하지 마.
            """;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/chats", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> postChats2(@RequestBody String message, Authentication authentication) {
        return chatClient.prompt()
                .advisors(new ExecutionTimeAdvisor())
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                //.advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .advisors(QuestionAnswerAdvisor
                        .builder(vectorStore)
                        .searchRequest(SearchRequest.builder().similarityThreshold(0.8).build())
                        .build())
                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        authentication.getName()
                ))
                .tools(productOrderTool)
                .toolContext(Map.of("username", authentication.getName()))
                .options(ChatOptions.builder().model("gpt-5.4"))
                .system(systemMessage)
                .user(message)
                .stream()
                .content()
                .map(objectMapper::writeValueAsString);
    }
}
