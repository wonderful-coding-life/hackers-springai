# frontend-prd.md

# 캠퍼스 고객센터 프론트엔드 구현 작업 지시서

## 목적

캠퍼스 고객센터 AI 챗봇의 웹 프론트엔드를 구현한다.

사용자는 로그인 후 AI 챗봇과 대화할 수 있어야 한다.

---

## 참고 문서

구현 시 반드시 다음 문서를 참고한다.

```text
docs/api-spec.md
docs/login-wireframe.md
docs/chat-wireframe.md
docs/design.md
```

각 문서에 정의된 내용을 구현 기준으로 사용한다.

중복 구현 기준을 만들지 말고 해당 문서를 우선적으로 따른다.

---

## 구현 범위

다음 두 개의 화면을 구현한다.

```text
1. 로그인 화면
2. 채팅 화면
```

- SPA로 구현하되 로그인 화면과 채팅 화면은 동시에 표시하지 않는다.
- 초기 진입 시 로그인 화면만 표시하고, 로그인 성공 후 채팅 화면으로 전환한다.
- 로그아웃 시 다시 로그인 화면으로 전환한다.

---

## 생성 파일

다음 파일을 생성한다.

```text
src/
 └─ main/
     └─ resources/
         └─ static/
             ├─ index.html
             ├─ css
             │  └─ chatbot.css
             └─ js
                └─ chatbot.js
```

---

## 기술 제약

다음 기술만 사용한다.

```text
HTML5
CSS3
Vanilla JavaScript
Fetch API
ReadableStream
```

다음 기술은 사용하지 않는다.

```text
React
Vue
Angular
TypeScript
jQuery
Node.js 기반 번들러
```

---

## 구현 요구사항

### 로그인 화면

`login-wireframe.md`를 기준으로 구현한다.

사용자가 입력한 인증 정보는 API 호출 시 사용한다.

---

### 채팅 화면

`chat-wireframe.md`를 기준으로 구현한다.

사용자 메시지와 챗봇 메시지는 서로 다른 말풍선 스타일로 표시한다.

---

### 디자인

`design.md`의 색상, 타이포그래피, 간격, 레이아웃 규칙을 따른다.

모바일 앱 스타일 UI를 구현한다.

---

### API 연동

`api-spec.md`를 기준으로 구현한다.

인증 방식, 요청 형식, 응답 형식, 스트리밍 처리 방식은 문서 정의를 그대로 따른다.

---

### 상태 관리

별도 프레임워크 없이 JavaScript만 사용한다.

필요한 최소 상태만 관리한다.

예시:

```javascript
{
  username,
  password,
  isStreaming,
  abortController
}
```

구현 방식은 자유롭게 결정할 수 있다.

---

## 사용자 경험 요구사항

다음 동작이 자연스럽게 이루어져야 한다.

```text
로그인
메시지 전송
스트리밍 응답 표시
응답 중단
로그아웃
오류 메시지 표시
```

---

## 코드 품질 요구사항

다음 원칙을 따른다.

```text
중복 코드 최소화
함수 단위 분리
명확한 네이밍 사용
주요 로직 주석 작성
ES6+ 문법 사용
```

---

## 최종 결과물

다음 파일 전체 코드를 생성한다.

```text
static/index.html
static/css/chatbot.css
static/js/chatbot.js
```

생성된 결과물은 별도 수정 없이 브라우저에서 실행 가능한 수준으로 완성한다.
