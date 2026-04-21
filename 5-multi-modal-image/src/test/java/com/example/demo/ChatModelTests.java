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
public class ChatModelTests {
    private static final Logger log = LoggerFactory.getLogger(ChatModelTests.class);

    @Autowired
    private OpenAiChatModel chatModel;

    @Test
    public void testMultiModalImage() {
        var resource = new ClassPathResource("images/Car.jpg");
        var mimeType = MimeTypeUtils.IMAGE_JPEG;
        var media = new Media(mimeType, resource);
        var userMessage = UserMessage.builder()
                .text("자동차 모델 이름 알려 줘.")
                .media(media)
                .build();
        var completions = chatModel.call(userMessage);
        log.info("{}", completions);
    }
}
