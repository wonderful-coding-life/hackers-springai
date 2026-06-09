# design.md

# 캠퍼스 고객센터 디자인 가이드

## 개요

캠퍼스 고객센터는 사용자가 AI 챗봇과 자연스럽게 대화할 수 있는 모바일 중심 고객지원 서비스이다.

전체 디자인은 복잡한 기업용 업무 시스템보다는 현대적인 메신저 스타일의 UI를 지향한다.

사용자가 처음 방문해도 쉽게 사용할 수 있도록 단순하고 직관적인 인터페이스를 제공한다.

---

# 디자인 키워드

* Mobile First
* Clean
* Modern
* Friendly
* Simple
* Conversational UI
* Customer Support
* SaaS Style
* ChatGPT Inspired

---

# 전체 레이아웃

## 모바일 우선

모든 화면은 모바일 앱 스타일을 기준으로 설계한다.

권장 최대 너비

```text
480px
```

데스크탑에서는 화면 중앙에 배치한다.

---

## 화면 구조

모든 페이지는 다음 구조를 따른다.

```text
Header
Content
Footer
```

채팅 화면에서는

```text
Header
Scrollable Chat Area
Message Input Area
```

구조를 사용한다.

---

# 컬러 시스템

## Primary Color

```css
#2563EB
```

주요 버튼
전송 버튼
링크
강조 요소

---

## Background Color

```css
#F8FAFC
```

전체 페이지 배경

---

## Surface Color

```css
#FFFFFF
```

카드
입력창
말풍선

---

## Border Color

```css
#E2E8F0
```

입력창
구분선
카드 테두리

---

## Text Color

### Primary

```css
#0F172A
```

### Secondary

```css
#64748B
```

### Placeholder

```css
#94A3B8
```

---

# 타이포그래피

## Font Family

```css
Pretendard
```

Fallback

```css
sans-serif
```

---

## 제목

```css
font-size: 28px;
font-weight: 700;
```

---

## 섹션 제목

```css
font-size: 18px;
font-weight: 600;
```

---

## 본문

```css
font-size: 15px;
font-weight: 400;
line-height: 1.6;
```

---

## 버튼

```css
font-size: 15px;
font-weight: 600;
```

---

# 간격 규칙

기본 spacing 단위

```css
4px
```

주요 사용 값

```css
4px
8px
12px
16px
24px
32px
```

---

# 모서리 반경

입력창

```css
12px
```

버튼

```css
12px
```

카드

```css
16px
```

말풍선

```css
18px
```

---

# 그림자

최소한만 사용한다.

```css
box-shadow:
0 1px 2px rgba(0,0,0,0.05);
```

과도한 그림자는 사용하지 않는다.

---

# 로그인 화면

## 레이아웃

세로 중앙 정렬

```text
로고 또는 아이콘

캠퍼스 고객센터

사용자 이름

패스워드

로그인 버튼
```

---

## 로그인 버튼

배경

```css
#2563EB
```

텍스트

```css
#FFFFFF
```

너비

```css
100%
```

---

# 채팅 화면

## 헤더

고정(Fixed)

높이

```css
64px
```

구성

```text
캠퍼스 고객센터
로그인 사용자 이름
로그아웃 버튼
```

---

## 채팅 영역

스크롤 가능

```css
overflow-y: auto;
```

메시지가 추가되면 자동으로 하단으로 이동한다.

---

# 챗봇 말풍선

위치

```text
좌측 정렬
```

배경

```css
#FFFFFF
```

테두리

```css
1px solid #E2E8F0
```

텍스트

```css
#0F172A
```

---

# 사용자 말풍선

위치

```text
우측 정렬
```

배경

```css
#2563EB
```

텍스트

```css
#FFFFFF
```

---

# 메시지 입력 영역

하단 고정

구성

```text
메시지 입력창
전송 버튼
```

---

## 입력창

높이

```css
48px
```

둥근 모서리

```css
24px
```

---

## 전송 버튼

원형 또는 둥근 사각형

배경

```css
#2563EB
```

아이콘

```text
Send
Arrow Up
Paper Plane
```

중 하나 사용 가능

---

# 로딩 상태

AI 응답 생성 중에는

```text
전송 버튼 → 중지 버튼
```

으로 변경한다.

---

# 애니메이션

부드럽고 짧게 사용한다.

```css
transition: 0.2s ease;
```

---

# 반응형 요구사항

모바일 우선 설계

지원 범위

```text
320px ~ 480px
```

태블릿과 데스크탑에서는 화면을 중앙에 배치한다.

---

# 접근성

모든 입력창은 label을 가진다.

모든 버튼은 hover와 focus 상태를 제공한다.

키보드만으로 모든 기능을 사용할 수 있어야 한다.

---

# 구현 가이드

Tailwind CSS 사용을 권장한다.

컴포넌트는 다음 단위로 분리한다.

```text
Header
LoginForm
ChatArea
ChatMessage
MessageInput
LogoutButton
```

전체 UI는 ChatGPT 스타일과 모바일 메신저 UI를 참고하되 더 단순하고 깔끔한 고객센터 서비스 느낌으로 구현한다.
