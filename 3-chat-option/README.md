# 3-chat-option

Spring AI의 `OpenAiChatModel`에서 `ChatOptions`를 지정하고, `ChatResponse`의 응답 본문과 메타데이터를 확인하는 예제 프로젝트입니다.

## 1. 프로젝트 생성

### 의존성

이 프로젝트는 Gradle 기반 Spring Boot 애플리케이션이며, 주요 설정은 다음과 같습니다.

- Java 21
- Spring Boot 4.1.0
- Spring AI 2.0.0
- OpenAI Chat Model Starter
- Lombok

`build.gradle`의 핵심 의존성은 다음과 같습니다.

```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.ai:spring-ai-bom:${springAiVersion}"
    }
}
```

### OPENAI_API_KEY 환경변수 설정

OpenAI API를 호출하려면 실행 환경에 `OPENAI_API_KEY`를 설정해야 합니다.

PowerShell:

```powershell
$env:OPENAI_API_KEY="your-api-key"
.\gradlew.bat bootRun
```

macOS/Linux:

```bash
export OPENAI_API_KEY="your-api-key"
./gradlew bootRun
```

환경변수가 설정되면 Spring AI의 OpenAI 자동 설정을 통해 `OpenAiChatModel` 빈이 생성되고, 애플리케이션 실행 시 OpenAI Chat API를 호출합니다.

## 2. ChatOptions

`OpenAiApplication`에서는 `OpenAiChatOptions`를 사용해 모델 호출 옵션을 지정합니다.

```java
var options = OpenAiChatOptions.builder()
        .model("gpt-5.4")
        .n(1)
        .temperature(1.0)
        .topP(1.0)
        .serviceTier("default")
        .reasoningEffort("low")
        .build();
```

각 옵션의 의미는 다음과 같습니다.

| 옵션 | 설명 |
| --- | --- |
| `model` | 사용할 OpenAI 모델 이름입니다. 예제에서는 `gpt-5.4`를 사용합니다. |
| `n` | 하나의 프롬프트에 대해 생성할 응답 후보 개수입니다. `1`이면 하나의 응답만 생성합니다. |
| `temperature` | 응답의 무작위성을 조절합니다. 값이 높을수록 더 다양한 표현이 생성되고, 낮을수록 더 일관적이고 예측 가능한 응답이 생성됩니다. |
| `topP` | 누적 확률 기반 샘플링 옵션입니다. `temperature`와 함께 응답 다양성을 조절하며, `1.0`은 후보 토큰 범위를 넓게 사용한다는 의미입니다. |
| `serviceTier` | OpenAI 요청 처리 티어를 지정합니다. 예제 주석 기준으로 `default`, `flex`, `priority` 값을 사용할 수 있습니다. |
| `reasoningEffort` | 추론 모델에서 추론에 사용할 노력을 지정합니다. 예제 주석 기준으로 `low`, `medium`, `high` 값을 사용할 수 있습니다. |

작성한 옵션은 `Prompt`에 연결해서 모델 호출에 사용합니다.

```java
var prompt = Prompt.builder()
        .messages(new UserMessage("사용자 메시지"))
        .chatOptions(options)
        .build();

var chatResponse = chatModel.call(prompt);
```

## 3. ChatResponse

`chatModel.call(prompt)`의 결과는 `ChatResponse`로 반환됩니다. `ChatResponse`에는 생성된 응답 결과와 응답 메타데이터가 포함됩니다.

### 응답 결과

```java
for (var result : chatResponse.getResults()) {
    log.info("{}", result.getOutput().getText());
}
```

- `getResults()`는 모델이 생성한 응답 후보 목록입니다.
- `result.getOutput()`은 생성된 메시지 객체입니다.
- `getText()`는 실제 응답 텍스트입니다.
- `n` 옵션을 1보다 크게 설정하면 여러 응답 후보가 포함될 수 있습니다.

### 응답 메타데이터

```java
var metadata = chatResponse.getMetadata();
```

메타데이터에서는 모델 정보, 토큰 사용량, 레이트 리밋 정보를 확인할 수 있습니다.

| 메타데이터 | 설명 |
| --- | --- |
| `metadata.getModel()` | 실제 응답을 생성한 모델 이름입니다. |
| `metadata.getUsage().getPromptTokens()` | 프롬프트 입력에 사용된 토큰 수입니다. |
| `metadata.getUsage().getCompletionTokens()` | 모델 응답 생성에 사용된 토큰 수입니다. |
| `metadata.getUsage().getTotalTokens()` | 입력 토큰과 출력 토큰을 합친 전체 토큰 수입니다. |
| `metadata.getRateLimit().getRequestsLimit()` | 요청 수 기준 전체 제한값입니다. |
| `metadata.getRateLimit().getRequestsRemaining()` | 요청 수 기준 남은 호출 가능 횟수입니다. |
| `metadata.getRateLimit().getRequestsReset()` | 요청 수 제한이 초기화되는 시점 또는 남은 시간 정보입니다. |
| `metadata.getRateLimit().getTokensLimit()` | 토큰 수 기준 전체 제한값입니다. |
| `metadata.getRateLimit().getTokensRemaining()` | 토큰 수 기준 남은 사용 가능 토큰입니다. |
| `metadata.getRateLimit().getTokensReset()` | 토큰 수 제한이 초기화되는 시점 또는 남은 시간 정보입니다. |

예제 코드에서는 다음과 같이 로그를 출력합니다.

```java
log.info("model = {}", metadata.getModel());
log.info("usage prompt = {}, completion = {}, total = {}",
        metadata.getUsage().getPromptTokens(),
        metadata.getUsage().getCompletionTokens(),
        metadata.getUsage().getTotalTokens());
log.info("request limit = {}, remaining = {}, reset = {}",
        metadata.getRateLimit().getRequestsLimit(),
        metadata.getRateLimit().getRequestsRemaining(),
        metadata.getRateLimit().getRequestsReset());
log.info("token limit = {}, remaining = {}, reset = {}",
        metadata.getRateLimit().getTokensLimit(),
        metadata.getRateLimit().getTokensRemaining(),
        metadata.getRateLimit().getTokensReset());
```

## 실행

```powershell
.\gradlew.bat bootRun
```

애플리케이션이 시작되면 `OpenAiApplication`의 `run` 메서드가 실행되고, 설정한 `ChatOptions`로 OpenAI 모델을 호출한 뒤 응답 텍스트와 메타데이터를 로그로 출력합니다.
