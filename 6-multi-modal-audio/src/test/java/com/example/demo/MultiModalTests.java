package com.example.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@SpringBootTest
public class MultiModalTests {
    private static final Logger log = LoggerFactory.getLogger(MultiModalTests.class);

    @Autowired
    private OpenAiChatModel chatModel;

    @Test
    public void testAudioInput() {
        var resource = new ClassPathResource("/audio/voc-kart-rider.mp3");
        var media = Media.builder()
                .data(resource)
                .mimeType(MimeTypeUtils.parseMimeType("audio/mp3"))
                .build();
        var userMessage = UserMessage.builder()
                .text("고객의 민원을 요약해 줘")
                .media(media).build();
        var chatOptions = OpenAiChatOptions.builder()
                .model("gpt-audio-mini")
                .build();
        var prompt = new Prompt(userMessage, chatOptions);
        var chatResponse = chatModel.call(prompt);
        log.info("{}", Objects.requireNonNull(chatResponse.getResult()).getOutput().getText());
    }

    @Test
    public void testAudioOutput() throws IOException {
        var chatOptions = OpenAiChatOptions.builder()
                .model("gpt-audio-mini")
                .outputModalities(List.of("text", "audio"))
                .outputAudio(new OpenAiApi.ChatCompletionRequest.AudioParameters(
                        OpenAiApi.ChatCompletionRequest.AudioParameters.Voice.ONYX,
                        OpenAiApi.ChatCompletionRequest.AudioParameters.AudioResponseFormat.MP3
                )).build();

        var prompt = new Prompt("스프링부트에 대해 짧게 설명해 주세요.", chatOptions);

        var chatResponse = chatModel.call(prompt);
        var assistantMessage = Objects.requireNonNull(chatResponse.getResult()).getOutput();
        log.info("{}", assistantMessage.getText());
        var audio = assistantMessage.getMedia().getFirst().getDataAsByteArray();
        Files.write(Paths.get("D:\\hackers\\lecture\\output\\springboot-onyx.mp3"), audio);
    }
}
