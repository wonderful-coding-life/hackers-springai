# design.md

# 캠퍼스 고객센터 디자인 가이드

## 목표

이 UI는 AI 고객센터 앱이다.

실제 상용 서비스에서 사용할 수 있을 정도의 완성도 있는 모바일 웹 UI를 목표로 한다.

핵심 키워드:

* Mobile App-like
* Premium
* Polished
* Production-ready
* Friendly
* Trustworthy
* Conversational
* Customer Support
* Modern Web App Style

---

# 전체 톤

화면에는 다음 요소를 적극적으로 사용한다.

* 카드형 모바일 프레임
* 상단 브랜드 영역
* 고객센터 상담원 느낌의 AI 프로필
* 메시지 시간
* 읽기 쉬운 말풍선
* 부드러운 그림자
* 하단 고정 입력 영역
* 빈 채팅 상태 안내
* 빠른 질문 버튼

---

# 레이아웃

## 데스크톱

데스크톱 브라우저에서는 모바일 앱 형태의 컨테이너를 화면 중앙에 배치한다.

```css
body {
  background: #EAF0F7;
}
```

앱 컨테이너는 브라우저 화면에 너무 꽉 차지 않도록 위아래 여백을 둔다.

```css
.app-container {
    width: 100%;
    max-width: 430px;
    height: calc(100vh - 64px);
    margin: 32px auto;
    background: #FFFFFF;
    border-radius: 28px;
    box-shadow: 0 24px 80px rgba(15, 23, 42, 0.16);
    overflow: hidden;
}
```

## 모바일

실제 모바일 화면에서는 앱 컨테이너가 화면 전체를 사용한다.

```css
@media (max-width: 767px) {
  body {
    background: #FFFFFF;
  }

  .app-container {
    width: 100%;
    max-width: none;
    height: 100vh;
    margin: 0;
    border-radius: 0;
    box-shadow: none;
  }
}
```

## 레이아웃 주의사항

* 데스크톱에서는 앱 컨테이너 위아래에 16px 이상의 여백을 둔다.
* 채팅 입력창은 브라우저 하단이 아니라 앱 컨테이너 내부 하단에 고정한다.
* Header와 Message Input은 고정하고, Chat Area만 스크롤되게 한다.
* 브라우저 배경에는 그라데이션을 사용하지 않는다.

## 화면 전환

* SPA 방식으로 구현하며 로그인 화면과 채팅 화면은 상태에 따라 하나만 표시한다.
* 로그인 화면과 채팅 화면이 동시에 표시되면 안 된다.

---

# 컬러 시스템

## Primary

```css
#2563EB
```

## Primary Gradient

```css
linear-gradient(135deg, #2563EB, #4F46E5)
```

## Page Background

```css
#EAF0F7
```

## Background

```css
#F8FAFC
```

## Chat Background

```css
#F1F5F9
```

## Surface

```css
#FFFFFF
```

## Text Primary

```css
#0F172A
```

## Text Secondary

```css
#64748B
```

## Border

```css
#E2E8F0
```

## Success

```css
#10B981
```

---

# 로그인 화면

로그인 화면은 단순 폼이 아니라 서비스 랜딩 카드처럼 보이게 만든다.

구성:

```text
상단 브랜드 아이콘
캠퍼스 고객센터
AI 상담으로 빠르게 문제를 해결하세요
로그인 카드
아이디 입력
비밀번호 입력
로그인 버튼
하단 안내 문구
```

브랜드 아이콘은 원형 그라데이션 배경에 채팅 아이콘을 배치한다.

로그인 카드는 흰색 배경, 둥근 모서리, 부드러운 그림자를 적용한다.

```css
border-radius: 24px;
box-shadow: 0 20px 50px rgba(15, 23, 42, 0.12);
```

로그인 버튼은 그라데이션을 사용한다.

```css
background: linear-gradient(135deg, #2563EB, #4F46E5);
height: 52px;
border-radius: 16px;
font-weight: 700;
```

---

# 채팅 화면

## 헤더

헤더는 고정 영역으로 만든다.

단순 텍스트 헤더가 아니라 실제 앱 헤더처럼 구성한다.

```text
[AI 프로필 아이콘] 캠퍼스 고객센터
온라인
[로그아웃]
```

AI 프로필 아이콘:

* 원형
* 파란색 그라데이션
* 채팅 아이콘
* 작은 초록색 온라인 표시 점

헤더 스타일:

```css
height: 72px;
background: rgba(255, 255, 255, 0.92);
backdrop-filter: blur(16px);
border-bottom: 1px solid #E2E8F0;
```

---

# 채팅 영역

채팅 영역 배경은 완전 흰색이 아니라 연한 회색 톤을 사용한다.

```css
background: #F1F5F9;
```

첫 진입 시 빈 화면에는 웰컴 카드를 보여준다.

```text
안녕하세요 👋
캠퍼스 고객센터 AI 상담원입니다.
무엇을 도와드릴까요?
```

웰컴 카드 아래에는 빠른 질문 버튼을 제공한다.

```text
[주문 목록 조회]
[반품 규정]
```

빠른 질문 버튼은 pill 형태로 만든다.

---

# 메시지 말풍선

## 공통

말풍선에는 다음을 포함한다.

```text
메시지 본문
작은 시간 표시
```

---

## AI 메시지

AI 메시지는 좌측 정렬한다.

AI 프로필 아이콘을 함께 표시한다.

```css
background: #FFFFFF;
color: #0F172A;
border: 1px solid #E2E8F0;
border-radius: 20px 20px 20px 6px;
box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
```

---

## 사용자 메시지

사용자 메시지는 우측 정렬한다.

```css
background: linear-gradient(135deg, #2563EB, #4F46E5);
color: #FFFFFF;
border-radius: 20px 20px 6px 20px;
box-shadow: 0 6px 16px rgba(37, 99, 235, 0.25);
```

---

# 로딩 상태

전송 버튼은 중지 버튼으로 변경한다.

---

# 메시지 입력 영역

하단 입력 영역은 고정한다.

```css
background: rgba(255, 255, 255, 0.94);
backdrop-filter: blur(16px);
border-top: 1px solid #E2E8F0;
padding: 12px;
```

입력창은 pill 형태로 만든다.

```css
height: 48px;
border-radius: 999px;
background: #F8FAFC;
border: 1px solid #CBD5E1;
```

전송 버튼은 원형 버튼으로 만든다.

```css
width: 44px;
height: 44px;
border-radius: 999px;
background: linear-gradient(135deg, #2563EB, #4F46E5);
```

아이콘은 paper plane 또는 arrow up을 사용한다.

---

# 마이크로 인터랙션

다음 인터랙션을 반드시 포함한다.

* 버튼 hover 시 살짝 밝아짐
* 버튼 active 시 scale 0.97
* 입력창 focus 시 파란색 ring 표시
* 메시지 추가 시 fade-in
* 빠른 질문 버튼 hover 효과
* 로그아웃 버튼 hover 효과

---

# 접근성

모든 input에는 label을 제공한다.

아이콘 버튼에는 aria-label을 제공한다.

키보드만으로 로그인, 메시지 입력, 전송, 로그아웃이 가능해야 한다.

---

# 구현 기준

Tailwind CSS를 사용한다.


