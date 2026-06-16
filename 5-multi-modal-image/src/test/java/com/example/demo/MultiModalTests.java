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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.MimeTypeUtils;

import java.util.List;
import java.util.function.Consumer;

@SpringBootTest
public class MultiModalTests {
    private static final Logger log = LoggerFactory.getLogger(MultiModalTests.class);

    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void testMultiModalImage() {
        //Resource resource = new ClassPathResource("/images/car.jpg");
        //Resource resource = new FileSystemResource("/home/hackers/images/car.jpg");
        //Resource resource = new UrlResource("https://x.x.x.x/car.jpg");

        //resourceLoader.getResource("classpath:/images/car.jpg");
        //resourceLoader.getResource("file:/home/hackers/images/car.jpg");
        //resourceLoader.getResource("https://x.x.x.x/car.jpg");

        var resource = resourceLoader.getResource("classpath:/images/car.jpg");
        var mimeType = MimeTypeUtils.IMAGE_JPEG;
        var media = new Media(mimeType, resource);
        var userMessage = UserMessage.builder()
                .text("자동차 모델 이름 알려 줘.")
                //.text("사진 속의 풍경을 동시로 표현해 줘.")
                .media(media)
                .build();
        var completions = chatModel.call(userMessage);
        log.info("\n{}", completions);
    }

    @Test
    public void testMultiModalImageReceipts() {
        Media[] media = {
                Media.builder()
                        .data(new ClassPathResource("/images/receipt-1.jpg"))
                        .mimeType(MimeTypeUtils.IMAGE_JPEG)
                        .build(),
                Media.builder()
                        .data(new ClassPathResource("/images/receipt-2.jpg"))
                        .mimeType(MimeTypeUtils.IMAGE_JPEG)
                        .build()
        };

        var completion = chatClient.prompt()
                .user(spec -> spec
                        .text("영수증의 날짜, 상호, 금액을 표 형태로 정리해 주세요.")
                        .media(media))
                .call().content();
        log.info("\ncompletion={}", completion);
    }
}
