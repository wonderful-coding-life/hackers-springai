# API Specification

## Overview

이 애플리케이션은 쇼핑몰 고객센터 AI 챗봇이다.

사용자는 Basic Authentication으로 인증한 후 채팅 API를 호출할 수 있다.

AI 응답은 SSE(Server-Sent Events) 방식으로 스트리밍된다.

---

## Authentication

모든 API 요청은 Basic Authentication이 필요하다.

```http
Authorization: Basic {Base64(username:password)}
```

---

## Chat API

### Request

```http
POST /chats
```

### Headers

```http
Authorization: Basic {Base64(username:password)}
Content-Type: text/plain
Accept: text/event-stream
```

### Request Body

사용자가 입력한 메시지를 순수 텍스트로 전송한다.

예시:

```text
배송 상태를 확인해줘
```

---

## Response

### Content-Type

```http
text/event-stream
```

### SSE Data Format

서버는 AI 응답을 토큰 단위로 스트리밍한다.

예시:

```text
data: "안녕하세요"

data: " 고객님의"

data: " 주문 정보를"

data: " 확인해드리겠습니다."
```

각 `data` 값은 JSON 문자열이므로 클라이언트에서 `JSON.parse()` 후 화면에 이어 붙여야 한다.

---

## Client Requirements

클라이언트 구현 시 다음 규칙을 반드시 준수한다.

* `/chats`는 POST 방식으로 호출한다.
* 요청 본문은 JSON이 아닌 `text/plain` 문자열이다.
* Basic Authentication 헤더를 포함한다.
* 응답은 SSE 스트림으로 처리한다.
* 브라우저에서는 `EventSource` 대신 `fetch()`와 `ReadableStream`을 사용한다.
* 수신한 `data` 값은 `JSON.parse()`로 파싱한다.
* 파싱된 문자열을 기존 응답에 이어 붙여 실시간 채팅 UI를 구현한다.
* 요청 취소 기능은 `AbortController`를 사용한다.

---

## Error Response

### 401 Unauthorized

인증 실패

### 403 Forbidden

권한 없음

### 500 Internal Server Error

서버 오류

---

## Notes

서버는 인증된 사용자를 기준으로 대화 이력을 관리한다.

사용자는 이전 대화 내용을 이어서 질문할 수 있으며, AI는 관련 문서를 검색(RAG)하고 필요한 경우 주문 조회 기능을 호출할 수 있다.
