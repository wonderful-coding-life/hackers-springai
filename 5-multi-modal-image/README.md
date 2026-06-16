# Spring AI 멀티모달 이미지 예제

Spring Boot와 Spring AI 2.0.0을 사용해 이미지 파일을 OpenAI Chat Model에 전달하는 멀티모달 예제입니다.

- `/images`: `ChatModel`에 이미지 1장과 사용자 메시지를 전달합니다.
- `/receipts`: `ChatClient`에 영수증 이미지 여러 장을 전달합니다.
- 테스트 페이지: `/image.html`, `/receipt.html`

## 1. 프로젝트 생성

이 프로젝트는 Gradle 기반 Spring Boot 애플리케이션입니다.

### 주요 의존성

`build.gradle`에는 다음 구성이 포함되어 있습니다.

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.1.0'
    id 'io.spring.dependency-management' version '1.1.7'
}

ext {
    set('springAiVersion', "2.0.0")
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.ai:spring-ai-bom:${springAiVersion}"
    }
}
```

Java 21 toolchain을 사용합니다.

### OPENAI_API_KEY 환경변수 설정

Spring AI OpenAI Starter는 `OPENAI_API_KEY` 환경변수를 사용해 API 키를 읽을 수 있습니다.

Windows PowerShell:

```powershell
$env:OPENAI_API_KEY="sk-..."
.\gradlew.bat bootRun
```

macOS/Linux:

```bash
export OPENAI_API_KEY="sk-..."
./gradlew bootRun
```

환경변수를 영구 설정하려면 운영체제의 사용자 환경변수 설정에 `OPENAI_API_KEY`를 추가합니다.

### 첨부 파일 크기 설정

이미지 업로드를 위해 `src/main/resources/application.properties`에 multipart 제한을 설정합니다.

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
```

- `max-file-size`: 파일 1개의 최대 크기
- `max-request-size`: 한 요청에 포함되는 전체 multipart 데이터의 최대 크기

여러 이미지를 업로드하는 `/receipts` API는 전체 요청 크기가 `max-request-size`를 넘지 않아야 합니다.

## 2. ChatModel로 멀티모달 처리

`ApiController`의 `/images` 엔드포인트는 `OpenAiChatModel`을 직접 사용합니다.

```java
@PostMapping("/images")
public String postImages(@RequestParam("file") MultipartFile file,
                         @RequestParam("message") String message) throws IOException {

    var resource = file.getResource();
    var mimeType = MimeTypeUtils.parseMimeType(file.getContentType());

    var userMessage = UserMessage.builder()
            .text(message)
            .media(new Media(mimeType, resource))
            .build();

    return chatModel.call(userMessage);
}
```

처리 흐름은 다음과 같습니다.

1. 클라이언트가 `file`과 `message`를 multipart 요청으로 전송합니다.
2. 업로드된 파일을 `Resource`로 가져옵니다.
3. 파일의 Content-Type을 Spring `MimeType`으로 변환합니다.
4. `UserMessage`에 텍스트와 이미지 `Media`를 함께 담습니다.
5. `chatModel.call(userMessage)`로 모델을 호출합니다.

요청 예시:

```bash
curl -X POST http://localhost:8080/images \
  -F "file=@car.jpg" \
  -F "message=이 이미지에 있는 자동차를 설명해 주세요."
```

브라우저에서는 `http://localhost:8080/image.html` 페이지로 테스트할 수 있습니다.

## 3. ChatClient로 멀티모달 처리

`ApiController`의 `/receipts` 엔드포인트는 `ChatClient`의 fluent API를 사용합니다.

```java
@PostMapping("/receipts")
public String postReceipts(@RequestParam("file") List<MultipartFile> files) {
    var media = files.stream()
            .map(file -> Media.builder()
                    .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                    .data(file.getResource())
                    .build())
            .toArray(Media[]::new);

    return chatClient.prompt()
            .user(spec -> spec
                    .text("영수증의 날짜, 상호, 금액을 표 형태로 정리해 주세요.")
                    .media(media))
            .call()
            .content();
}
```

`ChatClient`는 `AiConfig`에서 빈으로 등록합니다.

```java
@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
```

### 여러 이미지 업로드

Spring AI 2.0.0의 `ChatClient`는 여러 이미지를 한 번에 전달할 수 있습니다.

`PromptUserSpec.media()` 메서드는 `Media...` 가변 인자를 사용하므로 `List<Media>` 대신 `Media[]` 배열로 변환하여 전달합니다.

```java
Media[] media = files.stream()
        .map(file -> Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                .data(file.getResource())
                .build())
        .toArray(Media[]::new);

String result = chatClient.prompt()
        .user(spec -> spec
                .text("영수증의 날짜, 상호, 금액을 표 형태로 정리해 주세요.")
                .media(media))
        .call()
        .content();
```

여러 장의 영수증 이미지를 함께 전달하면 AI가 모든 이미지를 분석하여 결과를 생성합니다.

요청 예시:

```bash
curl -X POST http://localhost:8080/receipts \
  -F "file=@receipt-1.jpg" \
  -F "file=@receipt-2.jpg"
```

브라우저에서는 `http://localhost:8080/receipt.html` 페이지로 테스트할 수 있습니다.

## 실행

```bash
./gradlew bootRun
```

Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

애플리케이션 실행 후 다음 주소를 사용할 수 있습니다.

- 이미지 분석 테스트: `http://localhost:8080/image.html`
- 영수증 분석 테스트: `http://localhost:8080/receipt.html`
