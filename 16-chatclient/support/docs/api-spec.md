# API Specification

## 1. 개요

본 문서는 AI Agent가 클라이언트 애플리케이션을 개발하기 위한 API 명세서이다.

서버는 Spring Boot 기반으로 구현되어 있으며, Spring Security의 Basic Authentication을 사용한다.

채팅 API는 AI 응답을 Server-Sent Events(SSE) 형식으로 스트리밍한다.

---

## 2. 인증 방식

### Basic Authentication

모든 채팅 API 요청은 Basic Authentication 인증이 필요하다.

클라이언트는 요청 헤더에 다음 값을 포함해야 한다.

```http
Authorization: Basic {Base64(username:password)}
```

예시:

```http
Authorization: Basic dXNlcjpwYXNzd29yZA==
```

---

## 3. 채팅 API

### AI 채팅 메시지 전송

사용자가 입력한 메시지를 서버에 전송하면, 서버는 AI 응답을 SSE 방식으로 스트리밍한다.

### Request

```http
POST /api/v2/chats
```

### Request Headers

```http
Authorization: Basic {Base64(username:password)}
Content-Type: text/plain
Accept: text/event-stream
```

### Request Body

요청 본문은 JSON이 아니라 순수 텍스트이다.

```text
배송 조회해줘
```

---

## 4. Response

### Response Headers

```http
Content-Type: text/event-stream
```

### Response Body

서버는 AI 응답을 스트리밍한다.

각 토큰은 JSON 문자열로 직렬화되어 전송된다.

예시:

```text
data: "안녕하세요"

data: "."

data: " 주문"

data: " 정보를"

data: " 확인해드릴게요."
```

클라이언트는 각 `data` 값을 수신한 뒤 JSON 문자열로 파싱해야 한다.

JavaScript 예시:

```javascript
const token = JSON.parse(event.data);
```

---

## 5. 클라이언트 구현 주의사항

### 5.1 EventSource 사용 제한

브라우저의 `EventSource`는 GET 요청만 지원한다.

이 API는 POST 요청을 사용하므로, 일반적인 `EventSource` 방식으로는 호출할 수 없다.

POST 기반 SSE 스트리밍을 처리하려면 `fetch()`와 `ReadableStream`을 사용해야 한다.

---

## 6. JavaScript 호출 예시

```javascript
async function sendMessage(username, password, message, onToken) {
  const auth = btoa(`${username}:${password}`);

  const response = await fetch("/api/v2/chats", {
    method: "POST",
    headers: {
      "Authorization": `Basic ${auth}`,
      "Content-Type": "text/plain",
      "Accept": "text/event-stream"
    },
    body: message
  });

  if (!response.ok) {
    throw new Error(`HTTP Error: ${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");

  let buffer = "";

  while (true) {
    const { value, done } = await reader.read();

    if (done) break;

    buffer += decoder.decode(value, { stream: true });

    const events = buffer.split("\n\n");
    buffer = events.pop();

    for (const event of events) {
      const line = event
        .split("\n")
        .find(line => line.startsWith("data:"));

      if (!line) continue;

      const rawData = line.replace(/^data:\s*/, "");

      if (!rawData || rawData === "[DONE]") continue;

      const token = JSON.parse(rawData);
      onToken(token);
    }
  }
}
```

---

## 7. 호출 예시

```javascript
sendMessage(
  "user",
  "password",
  "배송 상태를 알려줘",
  token => {
    console.log("AI Token:", token);
  }
);
```

---

## 8. 에러 응답

### 401 Unauthorized

인증 정보가 없거나 잘못된 경우 발생한다.

```http
HTTP/1.1 401 Unauthorized
```

### 403 Forbidden

인증은 되었지만 접근 권한이 없는 경우 발생할 수 있다.

```http
HTTP/1.1 403 Forbidden
```

### 500 Internal Server Error

서버 내부 오류 또는 AI 모델 호출 중 오류가 발생한 경우 발생할 수 있다.

```http
HTTP/1.1 500 Internal Server Error
```

---

## 9. 서버 동작 요약

서버는 인증된 사용자의 username을 기준으로 다음 작업을 수행한다.

* 채팅 메모리의 conversation id로 사용
* Tool Calling 실행 시 tool context의 username으로 전달
* 사용자별 대화 문맥 유지
* VectorStore 기반 RAG 검색 수행
* AI 응답을 SSE 스트림으로 반환

---

## 10. AI Agent 구현 요구사항

AI Agent는 클라이언트 코드를 생성할 때 다음 조건을 반드시 지켜야 한다.

* `/api/v2/chats`는 POST로 호출한다.
* 요청 본문은 JSON이 아니라 `text/plain` 문자열로 전송한다.
* Basic Authentication 헤더를 포함한다.
* 응답은 SSE 스트림으로 처리한다.
* 브라우저에서는 `EventSource` 대신 `fetch()`와 `ReadableStream`을 사용한다.
* 수신한 `data` 값은 `JSON.parse()`로 파싱한다.
* 파싱한 문자열 토큰을 기존 AI 응답 영역에 계속 이어 붙인다.
* 요청 중 중지 기능이 필요하면 `AbortController`를 사용한다.

---

## 11. AbortController 예시

```javascript
let abortController = null;

async function startChat(username, password, message, onToken) {
  abortController = new AbortController();

  const auth = btoa(`${username}:${password}`);

  const response = await fetch("/api/v2/chats", {
    method: "POST",
    headers: {
      "Authorization": `Basic ${auth}`,
      "Content-Type": "text/plain",
      "Accept": "text/event-stream"
    },
    body: message,
    signal: abortController.signal
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");

  let buffer = "";

  while (true) {
    const { value, done } = await reader.read();

    if (done) break;

    buffer += decoder.decode(value, { stream: true });

    const events = buffer.split("\n\n");
    buffer = events.pop();

    for (const event of events) {
      const line = event
        .split("\n")
        .find(line => line.startsWith("data:"));

      if (!line) continue;

      const rawData = line.replace(/^data:\s*/, "");

      if (!rawData || rawData === "[DONE]") continue;

      onToken(JSON.parse(rawData));
    }
  }
}

function stopChat() {
  if (abortController) {
    abortController.abort();
  }
}
```
