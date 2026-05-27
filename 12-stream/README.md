# Flux란?

`Flux`는 Project Reactor에서 제공하는 Reactive Stream 타입으로,  
0개 이상의 데이터를 비동기적으로 순차 전달하는 Publisher이다.

Spring WebFlux에서는:

- 논블로킹(Non-Blocking)
- 비동기(Asynchronous)
- 스트리밍(Streaming)

기반의 데이터 처리를 위해 사용한다.

AI 스트리밍 응답에서는 GPT가 생성하는 토큰(Token)을 실시간으로 전달할 때 주로 사용된다.

---

# chatModel.call() vs chatModel.stream()

| 항목 | `call()` | `stream()` |
|---|---|---|
| 응답 방식 | 전체 응답 완성 후 반환 | 토큰 단위 실시간 반환 |
| 반환 타입 | `String`, `ChatResponse` | `Flux<String>` |
| 사용자 경험 | 응답 완료까지 대기 | ChatGPT 같은 실시간 UX |
| 처리 방식 | 동기(Synchronous) | 비동기(Reactive Streaming) |
| 서버 처리 모델 | 전통적 요청/응답 | 논블로킹 스트리밍 |
| 구현 난이도 | 단순 | SSE + Reactive 처리 필요 |
| 적합한 용도 | 일반 REST API | AI 채팅/실시간 생성 |
| 장점 | 구현 단순, 안정적 | 빠른 체감 속도, 자연스러운 응답 |
| 단점 | 긴 응답 시 대기 발생 | 상태 관리 및 예외 처리 복잡 |

---

# `call()` 예시

```java
@GetMapping("/chat")
public String chat(@RequestParam String message) {
    return chatModel.call(message);
}