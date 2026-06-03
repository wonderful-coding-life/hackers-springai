# ChatModel과 ChatClient 비교

Spring AI에서 AI 모델을 호출하는 대표적인 방법은 `ChatModel`과 `ChatClient`를 사용하는 것이다.

`ChatModel`은 AI 모델을 직접 호출하는 저수준 API에 가깝고, `ChatClient`는 프롬프트 구성, 옵션 설정, Tool Calling, Advisor, Chat Memory, RAG 등을 좀 더 스프링 스타일의 Fluent API로 사용할 수 있도록 제공하는 고수준 API이다.

단, ChatModel 이외의 다음과 같은 모델에서는 사용할 수 없다.

- ImageModel
- AudioSpeechModel
- AudioTranscriptionModel
- ModerationModel
- EmbeddingModel

---

# 1. 기본 개념 비교

| 구분                  | ChatModel   | ChatClient        |
| ------------------- | ----------- | ----------------- |
| 역할                  | AI 모델 직접 호출 | AI 모델 호출용 고수준 API |
| API 스타일             | Prompt 중심   | Fluent API        |
| 일반 호출               | O           | O                 |
| 스트리밍                | O           | O                 |
| Tool Calling        | O           | O                 |
| Advisor             | X           | O                 |
| Chat Memory Advisor | X           | O                 |
| RAG Advisor         | X           | O                 |
| 요청/응답 후처리           | 직접 구현       | Advisor 활용        |
| 실무 활용성              | 보통          | 높음                |

---

# 2. 호출 방식 비교

## ChatModel

```java
Prompt prompt = new Prompt(
        List.of(
                new UserMessage("Spring AI를 설명해줘")
        )
);

ChatResponse response = chatModel.call(prompt);

String content = response.getResult()
        .getOutput()
        .getText();
```

## ChatClient

```java
String content = chatClient.prompt()
        .user("Spring AI를 설명해줘")
        .call()
        .content();
```

`ChatClient`는 `Prompt`, `UserMessage`, `SystemMessage`를 직접 생성하지 않고도 프롬프트를 구성할 수 있다.

---

# 3. 주요 메서드 대응표

| 목적              | ChatModel                   | ChatClient              |
| --------------- | --------------------------- | ----------------------- |
| 일반 호출           | `call(prompt)`              | `.call()`               |
| 스트리밍 호출         | `stream(prompt)`            | `.stream()`             |
| 문자열 결과 조회       | 직접 추출                       | `.content()`            |
| ChatResponse 조회 | `ChatResponse` 반환           | `.chatResponse()`       |
| 사용자 메시지         | `new UserMessage()`         | `.user()`               |
| 시스템 메시지         | `new SystemMessage()`       | `.system()`             |
| 여러 메시지 추가       | `new Prompt(messages)`      | `.messages(messages)`   |
| 옵션 설정           | `Prompt(messages, options)` | `.options(...)`         |
| Tool 설정         | ChatOptions                 | `.tools(...)`           |
| Advisor 설정      | 지원 안 함                      | `.advisors(...)`        |
| 기본 Advisor 등록   | 지원 안 함                      | `.defaultAdvisors(...)` |

---

# 4. ChatResponse 조회

단순 문자열만 필요하다면 `.content()`를 사용할 수 있다.

```java
String content = chatClient.prompt()
        .user("Spring AI를 설명해줘")
        .call()
        .content();
```

메타데이터가 필요하다면 `ChatResponse`를 조회할 수 있다.

```java
ChatResponse response = chatClient.prompt()
        .user("Spring AI를 설명해줘")
        .call()
        .chatResponse();

System.out.println(response.getMetadata());
System.out.println(response.getResult().getMetadata());
```

---

# 5. Advisor란?

Advisor는 Spring MVC의 Filter 또는 Interceptor와 비슷한 역할을 수행한다.

AI 모델 호출 전후에 공통 로직을 수행할 수 있다.

대표적인 활용 예는 다음과 같다.

* 수행 시간 측정
* 요청/응답 로깅
* Chat Memory
* RAG
* Guardrail
* 사용자별 옵션 변경

Advisor는 ChatClient에서만 사용할 수 있다.

```java
chatClient.prompt()
        .advisors(new ExecutionTimeAdvisor())
        .user("Spring AI를 설명해줘")
        .call();
```

---

# 6. ExecutionTimeAdvisor 예제

```java
@Slf4j
public class ExecutionTimeAdvisor
        implements CallAdvisor, StreamAdvisor {

    @Override
    public String getName() {
        return "execution-time-advisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain) {

        long start = System.currentTimeMillis();

        ChatClientResponse response =
                chain.nextCall(request);

        long elapsed =
                System.currentTimeMillis() - start;

        log.info("elapsed={}ms", elapsed);

        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(
            ChatClientRequest request,
            StreamAdvisorChain chain) {

        long start = System.currentTimeMillis();

        Flux<ChatClientResponse> responseFlux =
                chain.nextStream(request);

        return new ChatClientMessageAggregator()
                .aggregateChatClientResponse(
                        responseFlux,
                        response -> {
                            long elapsed =
                                    System.currentTimeMillis() - start;

                            log.info("elapsed={}ms", elapsed);
                        }
                );
    }
}
```

---

# 7. Advisor Context

Advisor에는 Context를 통해 값을 전달할 수 있다.

대표적인 예가 Chat Memory에서 사용하는 Conversation ID이다.

```java
chatClient.prompt()
        .advisors(a -> a.param(
                ChatMemory.CONVERSATION_ID,
                authentication.getName()
        ))
        .user("이전 대화를 기반으로 설명해줘")
        .call();
```

Advisor에서는 다음과 같이 조회할 수 있다.

```java
String conversationId =
        (String) request.context()
                .get(ChatMemory.CONVERSATION_ID);
```

---

# 8. Tool Calling

ChatClient는 Tool Calling을 간단하게 구성할 수 있다.

```java
chatClient.prompt()
        .tools(
                ToolCallbacks.from(productOrderTool)
        )
        .user("내 주문 상태 알려줘")
        .call();
```

실행 흐름

```text
사용자 질문
    ↓
AI 모델
    ↓
Tool 호출 결정
    ↓
Tool 실행
    ↓
AI 모델
    ↓
최종 응답
```

---

# 9. Tool Context

실무에서는 Tool이 현재 로그인 사용자 정보를 알아야 하는 경우가 많다.

이러한 정보는 Tool Context를 통해 Tool에만 전달할 수 있다.

```java
chatClient.prompt()
        .tools(t -> t
                .callbacks(
                        ToolCallbacks.from(productOrderTool)
                )
                .context(
                        "username",
                        authentication.getName()
                )
        )
        .user("내 주문 상태 알려줘")
        .call();
```

Tool에서는 다음과 같이 사용할 수 있다.

```java
@Tool
public OrderStatus getOrderStatus(
        ToolContext toolContext) {

    String username =
            (String) toolContext.getContext()
                    .get("username");

    return orderService.getOrderStatus(username);
}
```

중요한 점은

```java
authentication.getName()
```

이 값은 AI 모델에게 전달되지 않는다.

오직 Tool 호출 시에만 사용할 수 있다.

---

# 10. Advisor Context와 Tool Context 비교

| 구분       | Advisor Context | Tool Context |
| -------- | --------------- | ------------ |
| 전달 대상    | Advisor         | Tool         |
| 설정 위치    | `.advisors()`   | `.tools()`   |
| 대표 사용 예  | Conversation ID | Username     |
| AI 모델 노출 | X               | X            |

Advisor Context

```java
.advisors(a -> a.param(
        ChatMemory.CONVERSATION_ID,
        authentication.getName()
))
```

Tool Context

```java
.tools(t -> t
        .callbacks(
                ToolCallbacks.from(productOrderTool)
        )
        .context(
                "username",
                authentication.getName()
        )
)
```

---

# 11. MessageChatMemoryAdvisor

Chat Memory는 직접 구현할 수도 있다.

```java
List<Message> history =
        chatMemory.get(conversationId);

chatClient.prompt()
        .messages(history)
        .user("이전 내용을 바탕으로 다시 설명해줘")
        .call();
```

하지만 스트리밍에서는 응답을 모두 모아서 저장해야 한다.

```java
StringBuilder answer =
        new StringBuilder();

chatClient.prompt()
        .user("Spring AI를 설명해줘")
        .stream()
        .chatResponse()
        .doOnNext(response -> {
            answer.append(
                    response.getResult()
                            .getOutput()
                            .getText()
            );
        })
        .doOnComplete(() -> {
            chatMemory.add(
                    conversationId,
                    new AssistantMessage(
                            answer.toString()
                    )
            );
        });
```

Spring AI는 이러한 작업을 자동화하기 위해 `MessageChatMemoryAdvisor`를 제공한다.

```java
@Bean
ChatClient chatClient(
        ChatClient.Builder builder,
        ChatMemory chatMemory) {

    return builder
            .defaultAdvisors(
                    MessageChatMemoryAdvisor
                            .builder(chatMemory)
                            .build()
            )
            .build();
}
```

사용 시에는 Conversation ID만 전달하면 된다.

```java
chatClient.prompt()
        .advisors(a -> a.param(
                ChatMemory.CONVERSATION_ID,
                authentication.getName()
        ))
        .user("이전 대화를 기반으로 설명해줘")
        .call();
```

Advisor가 자동으로 처리한다.

* 이전 대화 조회
* 프롬프트에 대화 이력 추가
* 사용자 메시지 저장
* Assistant 메시지 저장
* 스트림 응답 Aggregation

---

# 12. QuestionAnswerAdvisor (RAG)

Spring AI는 RAG 구현을 위한 `QuestionAnswerAdvisor`를 제공한다.
이를 위해 build.gradle에 다음과 같이 추가한다.

```properties
implementation 'org.springframework.ai:spring-ai-advisors-vector-store'
```

그러면 간단하게 vectorStore를 사용하여 RAG advisor를 만들 수 있다.

```java
chatClient.prompt()
        .advisors(
                QuestionAnswerAdvisor
                        .builder(vectorStore)
                        .build()
        )
        .user("환불 정책 알려줘")
        .call()
        .content();
```

이 Advisor는 내부적으로 다음 작업을 수행한다.

```text
사용자 질문
    ↓
VectorStore 검색
    ↓
관련 문서 조회
    ↓
프롬프트에 문서 추가
    ↓
AI 모델 호출
```

예를 들어

```text
환불 정책 알려줘
```

라는 질문이 들어오면

VectorStore에서

```text
환불은 결제일로부터 7일 이내 가능합니다.
전자제품은 개봉 후 환불이 불가능합니다.
```

와 같은 문서를 검색한 뒤

실제 모델에는 다음과 유사한 프롬프트가 전달된다.

```text
다음 문서를 참고하여 답변하세요.

환불은 결제일로부터 7일 이내 가능합니다.

전자제품은 개봉 후 환불이 불가능합니다.

질문:
환불 정책 알려줘
```

즉 RAG의 핵심인

```text
검색
+
프롬프트 증강
```

을 Advisor가 자동으로 수행한다.

---

# 13. 정리

`ChatModel`은 AI 모델을 직접 호출하는 기본 API이다.

반면 `ChatClient`는 Spring 스타일의 Fluent API를 제공하며 다음과 같은 장점을 가진다.

* `.user()`, `.system()`을 통한 직관적인 프롬프트 구성
* `.tools()`를 통한 Tool Calling 구성
* Tool Context 지원
* Advisor 기반의 요청/응답 처리
* 수행 시간 측정, 로깅 구현
* Chat Memory 지원
* RAG 지원
* Stream 응답 Aggregation 처리

특히 Chat Memory나 RAG를 직접 구현하면 프롬프트 조작, 대화 저장, 스트리밍 응답 집계 등의 코드가 필요하지만, `MessageChatMemoryAdvisor`와 `QuestionAnswerAdvisor`를 사용하면 Advisor 추가만으로 이러한 기능을 쉽게 적용할 수 있다.

따라서 실제 Spring AI 애플리케이션에서는 대부분 `ChatModel`보다 `ChatClient`를 사용하는 것이 더 편리하고 확장성이 높다.
