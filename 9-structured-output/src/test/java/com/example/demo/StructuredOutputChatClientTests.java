package com.example.demo;

import com.example.demo.ocr.ReceiptOcr;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

@SpringBootTest
public class StructuredOutputChatClientTests {
    private static final Logger log = LoggerFactory.getLogger(StructuredOutputChatClientTests.class);

    @Autowired
    private ChatClient chatClient;

    @Test
    public void testStructuredOutput() {
        Media[] media = {
                Media.builder()
                        .data(new ClassPathResource("/images/receipt-1.jpg"))
                        .mimeType(MimeTypeUtils.IMAGE_JPEG).build(),
                Media.builder()
                        .data(new ClassPathResource("/images/receipt-2.jpg"))
                        .mimeType(MimeTypeUtils.IMAGE_JPEG).build()
        };

        var completion = chatClient.prompt()
                .system("날짜는 LocalDate 시간은 LocalTime 날짜시간은 LocalDateTime 형식으로 바꿔 주세요")
                .user(spec -> spec
                        .text("영수증 이미지에서 정보를 추출해 주세요.")
                        .media(media))
                .call()
                .entity(new ParameterizedTypeReference<List<ReceiptOcr>>() {});

        log.info("\n{}", completion);
    }
}
