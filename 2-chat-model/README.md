# Spring AI ChatModel 예제

Spring Boot와 Spring AI OpenAI ChatModel을 사용해 GPT 응답을 생성하는 예제 프로젝트입니다.

## 1. 프로젝트 생성

Spring Initializr에서 다음 의존성을 선택해 생성한 프로젝트입니다.

- Spring Web
- OpenAI
- Lombok

현재 프로젝트에는 화면 예제를 위해 Thymeleaf도 추가되어 있습니다.

주요 설정은 [build.gradle](build.gradle)에 정의되어 있습니다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-webmvc'
implementation 'org.springframework.ai:spring-ai-starter-model-openai'
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
implementation 'org.commonmark:commonmark:0.28.0'
```

## 실행 준비

OpenAI API Key를 환경 변수로 설정합니다.

```bash
OPENAI_API_KEY=your-api-key
```

## 2. ChatModel 연동 - 기본 사용법

기본 사용 예제는 [ApiController.java](src/main/java/com/example/demo/controller/ApiController.java)를 참고합니다.

```java
@RestController
public class ApiController {

    @Autowired
    private OpenAiChatModel chatModel;

    @PostMapping("/chats")
    public String postChats(@RequestBody String message) {
        return chatModel.call(message);
    }
}
```

`OpenAiChatModel`을 주입받은 뒤 `chatModel.call(message)`를 호출하면 사용자의 메시지를 GPT에 전달하고 생성된 답변을 문자열로 받을 수 있습니다.

요청 예시:

```bash
curl -X POST http://localhost:8080/chats \
  -H "Content-Type: text/plain" \
  -d "Spring AI가 무엇인지 한 문장으로 설명해줘"
```

## 3. ChatModel 연동 - PromptTemplate과 SystemMessage

프롬프트를 구조화해서 사용하는 예제는 [MarketingController.java](src/main/java/com/example/demo/controller/MarketingController.java)를 참고합니다.

`SystemMessage`는 GPT가 어떤 역할과 규칙을 따라야 하는지 정의할 때 사용합니다.

```java
private static final Message systemMessage = new SystemMessage("""
    너는 전문 마케팅 카피라이터야.
    입력된 상품 정보를 기반으로 온라인 쇼핑몰 블로그 홍보 페이지에 사용할
    매력적인 마케팅 문구를 작성해 줘.
    Markdown 형식으로 작성해 주세요.
""");
```

`PromptTemplate`은 사용자가 입력한 값을 프롬프트에 안전하게 끼워 넣을 때 사용합니다.

```java
private static final PromptTemplate promptTemplate = new PromptTemplate("""
    ### 입력 정보
    - 상품명: {name}
    - 가격: {price}
    - 구매 링크: {link}
    - 상품 특징: {features}
""");
```

POST 요청에서는 템플릿에 실제 값을 넣어 사용자 메시지를 만들고, 시스템 메시지와 함께 ChatModel에 전달합니다.

```java
var userMessage = promptTemplate.createMessage(Map.of(
        "name", name,
        "price", price,
        "link", link,
        "features", features));

String result = chatModel.call(systemMessage, userMessage);
model.addAttribute("result", result);
```

브라우저에서 다음 주소로 접속하면 마케팅 문구 생성 화면을 사용할 수 있습니다.

```text
http://localhost:8080/marketing
```

## 4. GPT 생성 답변과 Markdown 처리

일반적으로 GPT는 제목, 목록, 강조 표현 등을 포함한 Markdown 포맷으로 답변을 생성하는 경향이 있습니다.

처리 방법은 크게 두 가지입니다.

1. 프롬프트에 "Markdown을 사용하지 말고 순수한 텍스트로 답변해줘"처럼 명시합니다.
2. Markdown 응답을 HTML로 변환한 뒤 화면에서 HTML로 렌더링합니다.

이 프로젝트는 두 번째 방식의 예제를 포함합니다.

[build.gradle](build.gradle)에 CommonMark 라이브러리를 추가합니다.

```gradle
implementation 'org.commonmark:commonmark:0.28.0'
```

[MarketingController.java](src/main/java/com/example/demo/controller/MarketingController.java)에서는 GPT가 생성한 Markdown 문자열을 HTML로 변환해 모델에 담습니다.

```java
Parser parser = Parser.builder().build();
Node document = parser.parse(result);
HtmlRenderer renderer = HtmlRenderer.builder().build();
String html = renderer.render(document);
model.addAttribute("html", html);
```

[marketing-response.html](src/main/resources/templates/marketing-response.html)에서는 원본 Markdown 문자열과 HTML 변환 결과를 모두 확인할 수 있습니다.

```html
<div class="result" th:text="${result}">결과가 여기에 표시됩니다.</div>
<div class="result" th:utext="${html}">결과가 여기에 표시됩니다.</div>
```

`th:text`는 HTML 태그를 문자 그대로 출력하고, `th:utext`는 HTML 태그를 해석해서 렌더링합니다. GPT 응답을 HTML로 표시할 때는 신뢰할 수 없는 입력이 그대로 노출되지 않도록 서비스 환경에 맞는 검증 또는 정제 처리를 함께 고려해야 합니다.
