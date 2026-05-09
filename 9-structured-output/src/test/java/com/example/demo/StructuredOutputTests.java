package com.example.demo;

import com.example.demo.ocr.ReceiptOcr;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

import java.text.MessageFormat;
import java.util.List;

@SpringBootTest
public class StructuredOutputTests {
    private static final Logger log = LoggerFactory.getLogger(StructuredOutputTests.class);

    @Autowired
    private OpenAiChatModel chatModel;

    @Test
    public void testStructuredOutput() {

        // BeanOutputConverter<ReceiptOcr> beanOutputConverter = new BeanOutputConverter<>(ReceiptOcr.class);
        BeanOutputConverter<List<ReceiptOcr>> beanOutputConverter =
                new BeanOutputConverter<>(
                        new ParameterizedTypeReference<List<ReceiptOcr>>() {}
                );

        log.info("\nformat: {}", beanOutputConverter.getFormat());
        log.info("\njsonSchema: {}", beanOutputConverter.getJsonSchema());

        String message = MessageFormat.format("""
                영수증 이미지에서 정보를 추출해 주세요.
                - issuedDate는 LocalDateTime 형식으로 바꿔 주세요.
                {0}
                """, beanOutputConverter.getFormat());

        var media = List.of(
                Media.builder()
                        .data(new ClassPathResource("/images/receipt-1.jpg"))
                        .mimeType(MimeTypeUtils.IMAGE_JPEG).build(),
                Media.builder()
                        .data(new ClassPathResource("/images/receipt-2.jpg"))
                        .mimeType(MimeTypeUtils.IMAGE_JPEG).build());

        var userMessage = UserMessage.builder()
                .text(message)
                .media(media)
                .build();
        var chatResponse = chatModel.call(new Prompt(userMessage));
        var json = chatResponse.getResult().getOutput().getText();
        var receiptOcrs = beanOutputConverter.convert(json);

        log.info("\n{}", receiptOcrs);
    }
}
