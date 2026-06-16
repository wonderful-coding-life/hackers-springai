# Image Models
- 과거에는 DALL·E 2, DALL·E 3 모델이 사용되었으나, 이러한 DALL·E 계열 모델은 2026년 5월 12일 이후 더 이상 지원되지 않습니다.
- 대신 GPT Image models가 도입되어 이미지 생성 및 관련 작업을 수행할 수 있습니다.
- DALL·E 모델은 기본적으로 url 형식의 결과를 반환하며, 옵션을 통해 b64_json 형식으로도 응답을 받을 수 있습니다.
- 반면 GPT Image 모델은 별도의 옵션 없이 항상 b64_json 형식으로 이미지를 반환합니다.
- 일부 최신 GPT Image 모델은 보호(protected) 모델로 분류되어 있으며, 사용을 위해서는 계정에 대한 신분 인증 절차가 필요할 수 있습니다.
- 모델별로 지원하는 이미지 크기, 품질 등의 옵션이 상이하므로, 사용 시 각 모델의 공식 문서를 참고하는 것이 필요합니다.
- 생성된 각 이미지에는 Revised Prompt가 메타데이터로 포함되어 있어, 이미지 생성 과정에서 실제로 사용된 프롬프트를 확인할 수 있습니다.

# 프롬프트 작성
GPT Image 프롬프트는 "무엇을 그릴지"보다:
- 어떤 분위기인지
- 어떤 촬영 느낌인지
- 어떤 용도인지
- 어떤 스타일인지

를 구체적으로 적는 것이 중요하고, 한글도 잘 이해하지만, 현재는 영어 프롬프트가:

- 스타일 표현
- 광고 감성
- 카메라 연출
- 조명
- 질감

같은 부분에서 더 안정적이고 디테일하게 나오는 경우가 많음.

용도 명확, 브랜드 요구사항 구체적, 스타일 키워드 좋음, 조명과 분위기 표현 좋음.
```text
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
```

장면 + 분위기 + 스타일 + 브랜드가 잘 설명 
```text
화성 표면에서 탐사 로버가 움직이고 있으며, 그 옆에는 2족 보행 로봇이 함께 탐사 활동을 하고 있다.
탐사 로버와 2족 보행 로봇에는 모두 "Hacker's Campus" 로고와 브랜드명이 선명하게 표시되어 있다.
붉은 모래 언덕과 먼지 낀 하늘이 배경이며, 태양빛이 낮게 비추는 오후의 분위기.
실제 사진처럼 보이는 고해상도 장면, 자연스러운 그림자와 질감, 시네마틱한 우주 탐사 분위기, 사실적인 금속 재질 표현.
```

```text
A realistic Mars exploration scene.
A futuristic rover is moving across the Martian surface, while a humanoid biped robot is exploring beside it.
Both the rover and the humanoid robot prominently display the brand name and logo "Hacker's Campus".
Red sand dunes and a dusty Martian sky in the background, with low afternoon sunlight casting long natural shadows.
Ultra realistic photography style, cinematic space exploration atmosphere, detailed metallic textures, natural lighting, high-resolution image.

```
# OpenAI SDK 사용

Spring AI의 `ImageModel`은 OpenAI, Stability AI, Vertex AI 등  
다양한 이미지 모델에 대한 공통 추상화를 제공합니다.

하지만 OpenAI의 Image Edit와 같은 Provider 전용 고급 기능은  
아직 공통 인터페이스에 완전히 통합되지 않았습니다.

따라서 이러한 기능을 사용하려면 OpenAI SDK를 직접 사용해야 하며,  
다음과 같이 `OpenAIClient`를 Bean으로 등록하여 사용할 수 있습니다.
- `AppConfig.java` 참조
- `OpenAiClientTests.java` 참조

## 참고 사항

Spring AI 2.0.0에서 OpenAI 공식 Java SDK를 사용하여 Image Edit 기능을 구현하려면 OkHttp 클라이언트 모듈을 추가해야 합니다.

```groovy
implementation 'com.openai:openai-java-client-okhttp:4.39.1'
```

위 의존성이 없으면 `OpenAIClient` 인터페이스는 사용할 수 있지만, `OpenAIOkHttpClient` 클래스를 사용할 수 없습니다.

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
```

또한 현재 Spring AI의 `ImageModel`은 이미지 생성(Text-to-Image)은 지원하지만 Image Edit API는 지원하지 않으므로, 이미지 편집 기능은 OpenAI Java SDK의 OpenAIClient를 직접 사용해야 합니다.
