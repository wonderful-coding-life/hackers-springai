# api-spec.md

# 캠퍼스 고객센터 API 명세서

## 개요

이 애플리케이션은 캠퍼스 고객센터 AI 챗봇이다.

프론트엔드는 사용자가 입력한 메시지를 서버에 전송하고, 서버는 AI 응답을 스트리밍 방식으로 반환한다.

---

## 인증 방식

이 애플리케이션은 Basic Authentication을 사용한다.

인증이 필요한 API 요청에는 다음 헤더를 포함한다.

```http
Authorization: Basic {Base64(username:password)}
```

정적 리소스는 인증 없이 접근할 수 있다.

```text
/
 /index.html
 /css/**
 /js/**
 /images/**
 /icons/**
 /fonts/**
```

그 외의 API 요청은 인증이 필요하다.

---

## 채팅 API

### Endpoint

```http
POST /api/chats
```

### 설명

사용자가 입력한 채팅 메시지를 서버에 전송한다.

서버는 AI 응답을 SSE 형식으로 스트리밍한다.

---

## Request

### Headers

```http
Authorization: Basic {Base64(username:password)}
Content-Type: text/plain
Accept: text/event-stream
```

### Body

요청 본문에는 사용자가 입력한 메시지를 순수 텍스트로 전송한다.

JSON 형식으로 보내지 않는다.

예시:

```text
수강신청 기간이 언제인가요?
```

---

## Response

### Content-Type

```http
text/event-stream
```

### 응답 형식

서버는 AI 응답을 스트리밍 방식으로 전송한다.

각 응답 조각은 SSE의 `data` 값으로 전달된다.

예시:

```text
data: "안녕하세요."

data: " 수강신청"

data: " 기간을"

data: " 안내해드리겠습니다."
```

각 `data` 값은 JSON 문자열이다.

프론트엔드는 수신한 `data` 값을 `JSON.parse()`로 파싱한 뒤, 기존 챗봇 응답 말풍선에 이어 붙인다.

---

## 클라이언트 구현 규칙

프론트엔드는 다음 규칙을 따른다.

* `/api/chats`는 `POST` 방식으로 호출한다.
* 요청 본문은 JSON이 아닌 `text/plain` 문자열로 전송한다.
* 인증이 필요한 요청에는 Basic Authentication 헤더를 포함한다.
* 응답은 `text/event-stream` 형식으로 처리한다.
* 브라우저에서는 `EventSource`를 사용하지 않는다.
* `fetch()`와 `ReadableStream`을 사용해 스트리밍 응답을 처리한다.
* 수신한 `data` 값은 `JSON.parse()` 후 화면에 이어 붙인다.
* 응답 처리 중 사용자가 중단할 수 있도록 요청 취소 기능을 제공한다.

---

## 오류 응답

### 401 Unauthorized

인증 정보가 없거나 올바르지 않은 경우 발생한다.

### 403 Forbidden

인증은 되었지만 요청 권한이 없는 경우 발생한다.

### 500 Internal Server Error

서버 처리 중 오류가 발생한 경우 발생한다.

---

## 서버 처리 기준

서버는 인증된 사용자 이름을 기준으로 대화 이력을 구분한다.

사용자가 이전에 질문한 내용은 같은 사용자 대화 이력에 이어서 처리될 수 있다.

서버는 필요한 경우 다음 기능을 사용할 수 있다.

* 고객센터 문서 검색
* 주문 정보 조회 도구 호출

이 내용은 서버 내부 처리 방식이며, 프론트엔드는 API 명세에 정의된 요청과 응답 형식만 따른다.
