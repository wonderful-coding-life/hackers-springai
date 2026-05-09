# BeanOutputConverter 사용 방법

BeanOutputConverter는 AI의 응답을 Java 객체로 변환할 때 사용한다.
단일 객체를 받을 수도 있고, 여러 객체(List)를 받을 수도 있다.

## 1. 단일 객체 변환

영수증 정보를 하나만 추출하는 경우에는 클래스 타입을 직접 전달한다.

```java
BeanOutputConverter<ReceiptOcr> beanOutputConverter =
        new BeanOutputConverter<>(ReceiptOcr.class);
```

예를 들어 AI 응답이 다음과 같다면:
```json
{
  "store": "스타벅스",
  "amount": 6100
}
```

이를 ReceiptOcr 객체 하나로 변환할 수 있다.

### 특징
* 단일 JSON 객체를 변환
* Class<T> 타입 전달
* 가장 간단한 형태

## 2. 리스트 객체 변환

여러 개의 영수증을 한 번에 추출하는 경우에는 List<ReceiptOcr> 형태로 받아야 한다.
이 경우 Java Generic 타입 정보가 런타임에 사라지기 때문에 ParameterizedTypeReference를 사용해야 한다.

```java
BeanOutputConverter<List<ReceiptOcr>> beanOutputConverter =
        new BeanOutputConverter<>(
                new ParameterizedTypeReference<List<ReceiptOcr>>() {}
        );
```


예를 들어 AI 응답이 다음과 같다면:
```json
[
  {
    "store": "스타벅스",
    "amount": 6100
  },
  {
    "store": "맥도날드",
    "amount": 8500
  }
]
```

이를 List<ReceiptOcr> 형태로 변환할 수 있다.

### 특징
* JSON 배열(Array) 변환
* ParameterizedTypeReference 필요
* Generic 타입 정보를 유지 가능

## 3. 왜 ParameterizedTypeReference가 필요한가?

Java의 Generic은 런타임에 타입 정보가 제거(Type Erasure)된다.

즉:
```java
List<ReceiptOcr>
```

는 런타임에 단순히:

```java
List
```

만 남게 된다.
따라서 다음 코드는 사용할 수 없다.

```java
new BeanOutputConverter<>(List<ReceiptOcr>.class) // 불가능
```
대신 ParameterizedTypeReference를 사용하여 Generic 타입 정보를 유지한다.
```java
new ParameterizedTypeReference<List<ReceiptOcr>>() {}
```

## 4. 정리
|형태|사용 방식|
|---|--------|
|단일 객체|`new BeanOutputConverter<>(ReceiptOcr.class)`|
|리스트 객체|`new BeanOutputConverter<>(new ParameterizedTypeReference<List<ReceiptOcr>>() {})`|

# JSON을 Java 객체로 변환할 때 주의사항

AI Structured Output에서 생성된 JSON을 Java 객체로 변환할 때는 타입과 포맷을 명확하게 맞춰야 한다.
특히 날짜/시간 타입은 자주 오류가 발생하므로 프롬프트에 Java 클래스 정보를 직접 명시하는 것이 좋다.

## 1. Java 타입에 맞는 형식을 사용해야 한다

예를 들어 다음과 같은 Java 클래스가 있다고 가정하자.

```java
public class ReceiptOcr {
    private String store;
    private LocalDateTime issuedDate;
    private Long amount;
}
```

이 경우 issuedDate 는 Java의 LocalDateTime 형식에 맞는 문자열이어야 한다.

## 2. 프롬프트에 Java 타입을 명시하는 것이 좋다

명시하지 않거나 또는 단순히:
```text
ISO-8601 형식으로 반환하세요.
```
라고만 하면 AI가 timezone 정보를 추가하는 경우가 많다.

예:
```text
2026-05-08T13:27:31+09:00
```

하지만 LocalDateTime 은 timezone 정보를 저장하지 못한다.

따라서 다음처럼 Java 타입을 직접 설명하는 것이 더 안전하다.

추천 프롬프트
```text
- issuedDate는 Java의 LocalDateTime 형식으로 반환하세요.
- timezone(+09:00, Z)은 포함하지 마세요.
예시:
2026-05-08T13:27:31
```

## 3. 날짜 타입별 기대 형식
| Java 타입          | 기대 형식                       |
| ---------------- | --------------------------- |
| `LocalDate`      | `2026-05-08`                |
| `LocalDateTime`  | `2026-05-08T13:27:31`       |
| `OffsetDateTime` | `2026-05-08T13:27:31+09:00` |
