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

    private String prompt = """
            입력 이미지의 평범한 승용차를 원래 차량 형태와 비율은 유지하면서 전문 투어링 레이스카 스타일로 변경해 줘.
            
            배경은 독일의 뉘르부르크링(Nürburgring) 레이싱 트랙으로 설정하고, 고속 주행 중인 긴장감 넘치는 레이싱 장면으로 만들어 줘.
            
            자동차에는 다음과 같은 레이싱 튜닝 요소를 추가:
            - 공격적인 프론트 스플리터
            - 대형 리어 윙
            - 레이싱 휠과 로우 프로파일 타이어
            - 낮아진 서스펜션
            - 와이드 바디킷
            - 실제 GT 레이스카 같은 디테일한 스폰서 데칼
            
            차량 전체는 검정 + 빨강 계열의 강렬한 레이싱 데칼 디자인으로 꾸며 주고,
            차량 측면과 보닛에는 “Hackers”라는 문구를 크게 넣어 줘.
            
            “Hackers” 로고는 실제 레이싱 팀처럼 세련되고 미래적인 스타일로 표현해 줘.
            
            전체 장면은 헐리우드 레이싱 영화 스타일의 시네마틱 연출로:
            - 영화 같은 카메라 구도
            - 사실적인 모션 블러
            - 타이어 연기와 물 튀김 효과
            - 극적인 흐린 하늘
            - 젖은 아스팔트 반사
            - 현실적인 모터스포츠 촬영 느낌
            - 속도감과 긴장감 강조
            - 초현실적인 디테일과 조명
            
            최종 결과물은 실제 레이싱 영화 또는 최신 레이싱 게임 트레일러의 한 장면처럼 매우 사실적으로 만들어 줘.
            """;

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
                .model("gpt-image-1.5")
                .image(imageField)
                .prompt(prompt)
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
