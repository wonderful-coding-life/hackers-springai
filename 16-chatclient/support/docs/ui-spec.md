# 해커스캠퍼스 고객센터 채팅 UI 구현 명세

## 프로젝트 목표

Spring Boot 기반 백엔드 API는 이미 구현되어 있습니다.

본 작업의 목적은 프론트엔드 기술을 학습하는 것이 아니라, Spring AI 기반 REST API를 실제 모바일 앱처럼 보이는 화면에서 사용하는 예제를 만드는 것입니다.

Basic Auth 기반으로 보호되는 AI 채팅 API를 호출할 수 있는 모바일 앱 스타일의 웹 화면을 구현해 주세요.

앱 이름은 다음과 같습니다.

```text
해커스캠퍼스 고객센터
```

실제 로그인 API는 존재하지 않습니다.

사용자가 입력한 username/password는 이후 채팅 API 호출 시 Basic Auth 헤더를 생성할 때만 사용합니다.

---

# 백엔드 API 정보

## 채팅 API

```http
POST /api/v2/chats
```

### Request Header

```http
Authorization: Basic {Base64(username:password)}
Content-Type: text/plain
Accept: text/event-stream
```

### Request Body

```text
수강 신청 방법을 알려주세요.
```

---

# 스트리밍 응답 형식

서버는 토큰의 공백을 보존하기 위해 토큰을 JSON 문자열로 직렬화하여 SSE 형태로 전송합니다.

백엔드 예시:

```java
.map(token -> objectMapper.writeValueAsString(token))
```

예시 응답:

```text
data: "안녕하세요"

data: " 해커스캠퍼스"

data: " 고객센터입니다."
```

따라서 클라이언트는 `data:` 뒤의 값을 일반 문자열로 처리하면 안 됩니다.

반드시 다음 순서로 처리해야 합니다.

1. SSE 이벤트 파싱
2. data 추출
3. JSON.parse() 수행
4. 복원된 문자열을 그대로 화면에 누적

예시:

```javascript
const jsonText = line.substring(5).trimStart();
const token = JSON.parse(jsonText);
appendToAiMessage(token);
```

다음과 같은 코드는 사용하면 안 됩니다.

```javascript
appendToAiMessage(line.substring(5).trim());
```

이 경우 토큰 앞뒤 공백이 제거되어 문장이 깨질 수 있습니다.

---

# 화면 방향 및 디자인

모바일 앱을 타겟으로 합니다.

반드시 세로형(Portrait) 레이아웃으로 구현해 주세요.

디자인 요구사항:

* 실제 모바일 앱 같은 Look & Feel
* 고객센터 앱 스타일
* 밝은 테마
* 모바일 중심 UI
* 스마트폰 화면 느낌
* 세로로 긴 레이아웃
* 상단 헤더 고정
* 하단 입력창 고정
* 채팅 영역 스크롤
* 부드러운 그림자
* 둥근 모서리
* 사용자/AI 말풍선 색상 구분
* 반응형 지원

Tailwind CSS CDN 사용

---

# 로그인 화면

구성:

* 앱 제목
* 안내 문구
* 사용자명(username)
* 비밀번호(password)
* 로그인 버튼

동작:

* 로그인 API 호출 없음
* 입력값을 JavaScript 메모리에 저장
* localStorage 저장 금지
* sessionStorage 저장 금지
* 로그인 후 채팅 화면으로 이동

---

# 채팅 화면

## 헤더

표시 항목:

* 해커스캠퍼스 고객센터
* AI 상담원 상태 메시지
* 로그인 사용자명
* 로그아웃 버튼

## 채팅 영역

초기 진입 시:

```text
안녕하세요.
해커스캠퍼스 고객센터입니다.
무엇을 도와드릴까요?
```

메시지 표시:

* 사용자 메시지 → 오른쪽 말풍선
* AI 메시지 → 왼쪽 말풍선
* AI 아이콘 또는 상담원 아바타 표시
* 자동 스크롤
* 스트리밍 응답 실시간 누적 표시

## 입력 영역

* 메시지 입력창
* 전송 버튼
* Enter 전송
* Shift + Enter 줄바꿈

전송 중:

* 중복 전송 방지
* 버튼 비활성화

응답 완료:

* 다시 전송 가능

---

# JavaScript 구현 요구사항

EventSource 사용 금지

반드시 다음 방식을 사용해 주세요.

```javascript
fetch()
ReadableStream
response.body.getReader()
TextDecoder
```

---

# Basic Auth 생성

```javascript
const authHeader =
    "Basic " + btoa(username + ":" + password);
```

---

# 채팅 API 호출

```javascript
fetch("/api/v2/chats", {
    method: "POST",
    headers: {
        "Authorization": authHeader,
        "Content-Type": "text/plain",
        "Accept": "text/event-stream"
    },
    body: message
});
```

---

# SSE 처리 요구사항

반드시 구현해야 하는 기능:

* fetch() 사용
* POST 방식 사용
* ReadableStream 사용
* response.body.getReader() 사용
* TextDecoder 사용
* SSE 이벤트 파싱
* data: 추출
* JSON.parse() 수행
* 토큰 누적 출력
* 자동 스크롤
* 에러 처리

---

# 정적 리소스 생성 규칙

생성하는 모든 UI 관련 파일은 반드시 다음 경로 아래에 생성해 주세요.

```text
src/main/resources/static
```

디렉터리 구조:

```text
src/main/resources/static
├─ index.html
├─ css
│   └─ chatbot.css
├─ js
│   └─ chatbot.js
├─ images
├─ icons
└─ fonts
```

규칙:

* HTML → 루트
* CSS → css
* JavaScript → js
* 이미지 → images
* 아이콘 → icons
* 폰트 → fonts

예시:

```html
<link rel="stylesheet" href="/css/chatbot.css">

<script src="/js/chatbot.js"></script>
```

---

# Spring Security 수정 요구사항

기존 SecurityConfig가 이미 존재합니다.

새로운 SecurityConfig를 생성하지 마세요.

기존 SecurityFilterChain에 아래 permitAll 경로가 존재하는지 확인해 주세요.

```java
.requestMatchers(
        "/",
        "/index.html",
        "/css/**",
        "/js/**",
        "/images/**",
        "/icons/**",
        "/fonts/**"
).permitAll()
```

위 경로가 없다면 추가해 주세요.

주의사항:

* 기존 SecurityConfig 전체 재작성 금지
* 기존 인증 방식 유지
* 기존 보안 정책 유지
* /api/v2/chats 는 계속 인증 필요
* 정적 리소스만 permitAll 추가

---

# 개발 환경

개발자는 IntelliJ IDEA에서 GitHub Copilot Chat을 사용한다고 가정합니다.

제약사항:

* Windows PowerShell 기준
* React 사용 금지
* Vue 사용 금지
* Angular 사용 금지
* Vite 사용 금지
* npm install 사용 금지
* npm run dev 사용 금지
* CDN 방식만 사용
* Spring Boot 정적 리소스로 동작

---

# 코드 품질 요구사항

* 초급 Spring 개발자도 이해 가능
* 충분한 주석 포함
* 유지보수 가능한 구조
* 불필요한 복잡성 제거
* 실제 실행 가능해야 함
* username/password 저장 금지
* JavaScript 메모리에서만 사용

---

# 작업 완료 조건

설명만 제공하는 작업이 아닙니다.

반드시 다음 파일을 생성하거나 전체 교체해야 합니다.

```text
src/main/resources/static/index.html
src/main/resources/static/css/chatbot.css
src/main/resources/static/js/chatbot.js
```

필요 시 다음 경로도 사용할 수 있습니다.

```text
src/main/resources/static/images/*
src/main/resources/static/icons/*
src/main/resources/static/fonts/*
```

---

# 최종 결과물

반드시 다음을 모두 제공해야 합니다.

1. src/main/resources/static/index.html 전체 코드
2. src/main/resources/static/css/chatbot.css 전체 코드
3. src/main/resources/static/js/chatbot.js 전체 코드
4. 기존 SecurityConfig에 추가해야 할 permitAll 코드
5. 실행 방법 설명

위 항목 중 하나라도 누락되면 작업이 완료된 것이 아닙니다.

코드는 실제 실행 가능한 수준으로 완성해 주세요.
