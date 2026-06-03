package com.example.demo.controller;

import com.example.demo.advisor.ExecutionTimeAdvisor;
import com.example.demo.tool.ProductOrderTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
public class ApiController {
    @Autowired
    private OpenAiChatModel chatModel;
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
            정확히 알고 있는 사실에 근거해서 답변하고 모르는 것은 고객센터 02-537-5000으로 안내해.
            답변은 짥고 명료해.
            답변은 순수 텍스트(Plain Text) 형식으로 작성하고, Markdown 문법은 사용 금지.
            """;

    @PostMapping("/api/v1/chats")
    public String postChats(@RequestBody String message, Authentication authentication) {

        List<Message> messages = List.of(
                new UserMessage(message),
                new SystemMessage(systemMessage)
        );
        ToolCallback[] toolCallbacks = ToolCallbacks.from(productOrderTool);

        ChatOptions chatOptions = OpenAiChatOptions.builder()
                .toolCallbacks(toolCallbacks)
                .toolContext("username", authentication.getName())
                .build();

        Prompt prompt = new Prompt(messages, chatOptions);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/api/v2/chats", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> postChats2(@RequestBody String message, Authentication authentication) {
        return chatClient.prompt()
                .advisors(new ExecutionTimeAdvisor())
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID,
                        authentication.getName()
                ))
                .tools(t -> t
                        .callbacks(ToolCallbacks.from(productOrderTool))
                        .context("username", authentication.getName())
                )
                .options(ChatOptions.builder().model("gpt-5.4-nano"))
                .system(systemMessage)
                .user(message)
                .stream()
                .content()
                .map(objectMapper::writeValueAsString);
    }
}
