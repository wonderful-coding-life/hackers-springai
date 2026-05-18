package com.example.demo.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    // Spring AI의 ImageModel은 OpenAI, Stability AI, Vertex AI 등
    // 다양한 이미지 모델에 대한 공통 추상화를 제공한다.
    // 하지만 OpenAI의 Image Edit와 같은 Provider 전용 고급 기능은
    // 아직 공통 인터페이스에 완전히 통합되지 않았다.
    //
    // 따라서 이러한 기능을 사용하려면 OpenAI SDK를 직접 사용해야 하며,
    // 다음과 같이 OpenAIClient를 Bean으로 등록하여 사용할 수 있다.
    @Bean
    public OpenAIClient openAIClient(@Value("${spring.ai.openai.api-key}") String apiKey) {
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}
