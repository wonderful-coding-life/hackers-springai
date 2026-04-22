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

    // 리얼리즘 사진 스타일
    String message1 = """
            화성 표면에서 탐사 로버가 움직이고 있으며, 그 옆에는 2족 보행 로봇이 함께 탐사 활동을 하고 있다.
            붉은 모래 언덕과 먼지 낀 하늘이 배경이며, 태양빛이 낮게 비추는 오후의 분위기.
            실제 사진처럼 보이는 고해상도 장면, 자연스러운 그림자와 질감.
            """;

    // 시네마틱 영화 장면 스타일
    String message2 = """
            화성 표면에서 탐사 로버가 움직이고 있으며, 그 옆에는 2족 보행 로봇이 함께 탐사 활동을 하고 있다.
            붉은 모래 언덕과 먼지 낀 하늘이 배경이며, 태양빛이 낮게 비추는 오후의 분위기.
            영화 포스터처럼 웅장하고 드라마틱한 구도.
            """;

    // 과학 다큐멘터리 스타일
    String message3 = """
            화성 탐사 현장을 다큐멘터리 사진처럼 표현.
            실제 NASA 탐사 사진처럼 로버의 금속 질감과 먼지 낀 렌즈 표현이 사실적이다.
            2족 보행 로봇이 로버 옆에서 탐사를 돕는 장면.
            """;

    @Test
    public void testImageModel() {
        var response = imageModel.call(new ImagePrompt(message1));
        var url = Objects.requireNonNull(response.getResult()).getOutput().getUrl();
        log.info("\n{}", url);
    }

    @Test
    public void testImageModelWithOptions() throws IOException {
        var options = OpenAiImageOptions.builder()
                .model("gpt-image-1") // dall-e-3(will be deprecated on May 12, 2026), gpt-image-1-mini, gpt-image-2 (protected)
                .quality("medium") // high, medium, low
                //.responseFormat("b64_json") // url, b64_json (dall-e-xxx supports both, gpt-image-xxx supports only b64_json)
                .width(1024) // need to check the valid combinations of width and height for each model, otherwise it will throw an error
                .height(1024)
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

        var url = Objects.requireNonNull(response.getResult()).getOutput().getUrl();
        if (url != null) {
            log.info("\n{}", url);
        }

        // each generation has metadata with revised prompt which can be used for debugging and analysis
        // revised prompt is the final prompt that was used for generation after any internal modifications by the model
        var metadata = Objects.requireNonNull(response.getResult()).getMetadata();
        if (metadata instanceof OpenAiImageGenerationMetadata open) {
            log.info("\n{}", open.getRevisedPrompt());
        }
    }
}
