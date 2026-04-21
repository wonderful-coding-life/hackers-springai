package com.example.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

@SpringBootTest
public class MultiModalTests {
    private static final Logger log = LoggerFactory.getLogger(MultiModalTests.class);

    @Autowired
    private OpenAiChatModel chatModel;

    @Test
    public void testMultiModalImage() {
        //Resource resource = new ClassPathResource("/audio/voc_kart_rider.mp3");
        //Resource resource = new FileSystemResource("D:\\hackers\\lecture\\sample\\voc-kart-rider.mp3");
        //Resource resource = new UrlResource("https://xxx/sample_audio.mp3");

        //var resource = new ClassPathResource("images/car.jpg");
        var resource = new ClassPathResource("images/disney-world-2.jpg");
        var mimeType = MimeTypeUtils.IMAGE_JPEG;
        var media = new Media(mimeType, resource);
        var userMessage = UserMessage.builder()
                //.text("자동차 모델 이름 알려 줘.")
                .text("사진 속의 풍경을 동시로 표현해 줘.")
                .media(media)
                .build();
        var completions = chatModel.call(userMessage);
        log.info("\n{}", completions);
    }
}
