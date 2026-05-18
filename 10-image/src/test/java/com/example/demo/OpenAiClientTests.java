package com.example.demo;

import com.openai.client.OpenAIClient;
import com.openai.core.MultipartField;
import com.openai.models.images.ImageEditParams;
import com.openai.models.images.ImagesResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@SpringBootTest
public class OpenAiClientTests {
    @Autowired
    private OpenAIClient openAIClient;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void testImageEdit() throws IOException {
        var resource = resourceLoader.getResource("classpath:/images/car.jpg");
        MultipartField<ImageEditParams.Image> imageField =
                MultipartField.<ImageEditParams.Image>builder()
                        .value(ImageEditParams.Image.ofInputStream(resource.getInputStream()))
                        .filename("car.jpg")
                        .contentType("image/jpeg")
                        .build();

        ImageEditParams params = ImageEditParams.builder()
                .model("gpt-image-1-mini")
                .image(imageField)
                .prompt("자동차 색상을 초록색으로 바꿔 줘")
                .size(ImageEditParams.Size._1024X1024)
                .quality(ImageEditParams.Quality.AUTO)
                .build();

        ImagesResponse response = openAIClient.images().edit(params);

        String b64Json = response.data()
                .orElseThrow()
                .getFirst()
                .b64Json()
                .orElseThrow();
        byte[] imageBytes = Base64.getDecoder().decode(b64Json);
        Files.write(Paths.get("D:\\hackers\\lecture\\output\\openaisdk-image.png"), imageBytes);
    }
}
