package com.example.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.metadata.OpenAiImageGenerationMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Objects;

@SpringBootTest
public class ImageModelTests {
    private static final Logger log = LoggerFactory.getLogger(ImageModelTests.class);

    @Autowired
    private OpenAiImageModel imageModel;

    // 시네마틱 영화 장면 스타일
    String message1 = """
            화성 표면에서 탐사 로버가 움직이고 있으며, 그 옆에는 2족 보행 로봇이 함께 탐사 활동을 하고 있다.
            탐사 로버와 2족 보행 로봇에는 모두 "Hacker's Campus" 로고와 브랜드명이 선명하게 표시되어 있다.
            붉은 모래 언덕과 먼지 낀 하늘이 배경이며, 태양빛이 낮게 비추는 오후의 분위기.
            실제 사진처럼 보이는 고해상도 장면, 자연스러운 그림자와 질감, 시네마틱한 우주 탐사 분위기, 사실적인 금속 재질 표현.
            """;

    String message2 = """
            A realistic Mars exploration scene.
            A futuristic rover is moving across the Martian surface, while a humanoid biped robot is exploring beside it.
            Both the rover and the humanoid robot prominently display the brand name and logo "Hacker's Campus".
            Red sand dunes and a dusty Martian sky in the background, with low afternoon sunlight casting long natural shadows.
            Ultra realistic photography style, cinematic space exploration atmosphere, detailed metallic textures, natural lighting, high-resolution image.
            """;

    // 리얼리즘 사진 스타일
    String message3 = """
            Create a premium Instagram marketing banner for Hacker's Cafe summer coffee promotion.
            
            Requirements:
            - prominently feature the brand name "Hacker's Cafe"
            - modern premium cafe branding
            - iced coffee on marble table
            - warm natural sunlight
            - clean luxury typography layout
            - realistic commercial product photography
            - stylish cafe advertisement design
            - cinematic lighting
            - Instagram 4:5 aspect ratio
            - high-end lifestyle marketing style
            """;

    @Test
    public void testImageModelWithOptions() throws IOException {
        var options = OpenAiImageOptions.builder()
                .model("gpt-image-1-mini") // dall-e-3(will be deprecated on May 12, 2026), gpt-image-1-mini, gpt-image-2 (protected)
                .quality("medium") // high, medium, low
                .build();

        var response = imageModel.call(new ImagePrompt(message3, options));
        var b64Json = Objects.requireNonNull(response.getResult()).getOutput().getB64Json();
        if (b64Json != null) {
            log.info("\bBase64 JSON length: {}", b64Json.length());
            log.info("\n{}", b64Json);
            byte[] imageBytes = Base64.getDecoder().decode(b64Json);
            // support only png format for b64_json response, so we can save the file with .png extension
            Files.write(Paths.get("D:\\hackers\\lecture\\output\\openai-image.png"), imageBytes);
        }
    }
}
