# chatModel.call() vs chatModel.stream()

| 항목 | `call()` | `stream()` |
|---|---|---|
| 응답 방식 | 전체 응답 완성 후 반환 | 토큰 단위 실시간 반환 |
| 반환 타입 | `String`, `ChatResponse` | `Flux<String>` |
| 사용자 경험 | 응답 완료까지 대기 | ChatGPT 같은 실시간 UX |
| 구현 난이도 | 단순 | SSE + Reactive 처리 필요 |
| 서버 처리 방식 | 전통적 요청/응답 | 논블로킹 스트리밍 |
| 적합한 용도 | 일반 REST API | AI 채팅/실시간 생성 |
| 장점 | 구현 단순, 안정적 | 빠른 체감 속도, 자연스러운 응답 |
| 단점 | 긴 응답 시 대기 발생 | 상태 관리 및 예외 처리 복잡 |

---

# stream() 사용 시 주의사항

## Tool Calling과 함께 사용할 때 주의

사용자가 스트리밍 도중:

- 브라우저 종료
- 네트워크 끊김
- 요청 취소

할 수 있음.

하지만 이미:

- DB 변경
- 메일 발송
- 주문 처리
- 외부 API 호출

같은 Tool 실행은 완료되었을 수 있음.

즉:

```text
사용자는 실패로 인식
하지만 실제 작업은 수행 완료
```

---

## SSE(Server-Sent Events) 사용 시 주의사항

Spring WebFlux에서 `Flux<String>` 을 `text/event-stream` 형식으로 반환하면,
OpenAI 스트리밍 토큰이 실시간으로 브라우저에 전달된다.
하지만 OpenAI 스트리밍 토큰에는 앞 공백이 포함될 수 있으며,
SSE(EventSource) 처리 과정에서 앞 공백이 손실될 수 있다.
따라서 String을 직렬화 해서 보내야 하는데 `ObjectMapper.writeValueAsString()`은
단순히 문자열에 따옴표를 붙이는 것이 아니라, JSON 규칙에 맞게 escape 처리까지 수행하는 JSON 직렬화 기능이다.

서버에서
```java
return chatModel.stream(message).map(objectMapper::writeValueAsString);
```

클라이언트에서
```javascript
es.onmessage = (evt) => {
    appendMessage(JSON.parse(evt.data));
};
```