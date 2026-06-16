package com.example.demo;

import com.openai.models.completions.CompletionUsage;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@SpringBootTest
public class MultiModalChatClientTests {
    private static final Logger log = LoggerFactory.getLogger(MultiModalChatClientTests.class);

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void testAudioInput() {
        var resource = resourceLoader.getResource("classpath:/audio/voc-kart-rider.mp3");
        var media = Media.builder()
                .data(resource)
                .mimeType(MimeTypeUtils.parseMimeType("audio/mp3"))
                .build();
        var completion = chatClient.prompt()
                .options(OpenAiChatOptions.builder().model("gpt-audio"))
                .user(spec -> spec
                        .text("고객의 민원을 요약해 줘")
                        .media(media))
                .call().content();
        log.info("\ncompletion={}", completion);
    }

    @Test
    public void testAudioOutput() throws IOException {
        var chatResponse = chatClient.prompt()
                .options(OpenAiChatOptions.builder()
                        .model("gpt-audio")
                        .outputModalities(List.of("text", "audio"))
                        .outputAudio(new OpenAiChatOptions.AudioParameters(
                                OpenAiChatOptions.AudioParameters.Voice.ONYX,
                                OpenAiChatOptions.AudioParameters.AudioResponseFormat.MP3
                        )))
                .user("스프링부트에 대해 짧게 설명해 주세요.")
                .call().chatResponse();

        var assistantMessage = Objects.requireNonNull(chatResponse.getResult()).getOutput();
        log.info("\n{}", assistantMessage.getText());

        var audio = assistantMessage.getMedia().getFirst().getDataAsByteArray();
        Files.write(Paths.get("D:/hackers/lecture/output/springboot-onyx.mp3"), audio);

        var usage = (CompletionUsage)chatResponse.getMetadata().getUsage().getNativeUsage();
        log.info("native usage = {}", usage);
    }
}
