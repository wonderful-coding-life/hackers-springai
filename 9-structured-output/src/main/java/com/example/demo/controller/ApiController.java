package com.example.demo.controller;

import com.example.demo.entity.Receipt;
import com.example.demo.ocr.ReceiptOcr;
import com.example.demo.repository.ReceiptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.text.MessageFormat;
import java.util.List;

@RestController
@Slf4j
public class ApiController {
    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private ReceiptRepository receiptRepository;

    @PostMapping("/receipts")
    public List<ReceiptOcr> postReceipts(@RequestParam("file") List<MultipartFile> files) {
        var media = files.stream()
                .map(file -> Media.builder()
                        .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                        .data(file.getResource())
                        .build())
                .toList();

        BeanOutputConverter<List<ReceiptOcr>> beanOutputConverter =
                new BeanOutputConverter<>(
                        new ParameterizedTypeReference<List<ReceiptOcr>>() {}
                );

        String message = MessageFormat.format("""
                영수증 이미지에서 정보를 추출해 주세요.
                - issuedDate는 LocalDateTime 형식으로 바꿔 주세요.
                {0}
                """, beanOutputConverter.getFormat());
        var userMessage = UserMessage.builder()
                .text(message)
                .media(media)
                .build();
        var chatResponse = chatModel.call(new Prompt(userMessage));
        var json = chatResponse.getResult().getOutput().getText();
        var receiptOcrs = beanOutputConverter.convert(json);

        log.info("\n{}", receiptOcrs);
        var receipts = receiptOcrs.stream().map(Receipt::new).toList();
        receiptRepository.saveAll(receipts);

        return receiptOcrs;
    }

    @GetMapping("/receipts")
    public List<Receipt> getReceipts() {
        return receiptRepository.findAll();
    }
}
