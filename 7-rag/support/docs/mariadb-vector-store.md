## Spring AI MariaDB Vector Store SQL 예제

Spring AI의 MariaDB Vector Store는 문서 내용, 메타데이터, 임베딩 벡터를 함께 저장한다.

기본 구조는 다음과 같다.

```text
id        : 문서 ID
content   : 문서 본문
metadata  : 문서 메타데이터(JSON)
embedding : 임베딩 벡터
```

> 예제에서는 이해를 쉽게 하기 위해 `VECTOR(3)`을 사용한다.  
> 실제 OpenAI 임베딩 모델을 사용하면 보통 `VECTOR(1536)`을 사용한다.

### 1. 테이블 생성

```sql
DROP TABLE IF EXISTS vector_store;

CREATE TABLE vector_store (
    id VARCHAR(36) PRIMARY KEY,
    content TEXT NOT NULL,
    metadata JSON,
    embedding VECTOR(3) NOT NULL
);

CREATE VECTOR INDEX idx_vector_store_embedding
ON vector_store (embedding)
DISTANCE = cosine;
```

### 2. 데이터 입력

```sql
INSERT INTO vector_store (
    id,
    content,
    metadata,
    embedding
)
VALUES
(
    UUID(),
    'Spring AI는 스프링 애플리케이션에서 AI 모델을 쉽게 연동할 수 있도록 도와준다.',
    JSON_OBJECT(
        'category', 'spring-ai',
        'type', 'guide',
        'author', 'kim',
        'version', '2.0'
    ),
    VEC_FromText('[0.10, 0.20, 0.30]')
),
(
    UUID(),
    'RAG는 외부 문서를 검색한 뒤 검색 결과를 프롬프트에 함께 전달하는 방식이다.',
    JSON_OBJECT(
        'category', 'rag',
        'type', 'guide',
        'author', 'kim',
        'version', '2.0'
    ),
    VEC_FromText('[0.11, 0.19, 0.31]')
),
(
    UUID(),
    'Spring Security는 인증과 인가 기능을 제공하는 보안 프레임워크이다.',
    JSON_OBJECT(
        'category', 'security',
        'type', 'reference',
        'author', 'lee',
        'version', '1.0'
    ),
    VEC_FromText('[0.80, 0.10, 0.20]')
);
```

### 3. 데이터 조회

`VECTOR` 컬럼은 일반 조회 시 binary처럼 보일 수 있으므로 `VEC_ToText()`를 사용하면 숫자 배열 형태로 확인할 수 있다.

```sql
SELECT
    id,
    content,
    JSON_PRETTY(metadata) AS metadata,
    VEC_ToText(embedding) AS embedding
FROM vector_store;
```

### 4. 데이터 수정

```sql
UPDATE vector_store
SET
    content = 'Spring AI는 AI 모델 연동, RAG, Tool Calling 등을 지원한다.',
    metadata = JSON_OBJECT(
        'category', 'spring-ai',
        'type', 'guide',
        'author', 'kim',
        'version', '2.1'
    ),
    embedding = VEC_FromText('[0.12, 0.21, 0.29]')
WHERE JSON_VALUE(metadata, '$.category') = 'spring-ai';
```

### 5. metadata 일부 수정

```sql
UPDATE vector_store
SET metadata = JSON_SET(
    metadata,
    '$.version',
    '2.2',
    '$.updated',
    true
)
WHERE JSON_VALUE(metadata, '$.category') = 'spring-ai';
```

### 6. 데이터 삭제

```sql
DELETE FROM vector_store
WHERE JSON_VALUE(metadata, '$.category') = 'security';
```

### 7. 가까운 벡터 검색

사용자 질문도 임베딩 모델을 통해 벡터로 변환한 뒤, 저장된 문서 벡터와의 거리를 계산한다.

> 거리 계산에 사용되는 두 벡터는 반드시 동일한 차원을 가져야 한다.
> 예를 들어 `VECTOR(1536)` 컬럼에 저장된 벡터와 비교할 경우, 검색에 사용하는 벡터 역시 1536차원이어야 한다.
> 차원이 다르면 `VEC_DISTANCE_COSINE()` 함수는 `NULL`을 반환한다.

```sql
SELECT
    id,
    content,
    JSON_PRETTY(metadata) AS metadata,
    VEC_DISTANCE_COSINE(
        embedding,
        VEC_FromText('[0.10, 0.20, 0.32]')
    ) AS distance
FROM vector_store
ORDER BY distance
LIMIT 5;
```

`distance` 값이 작을수록 더 가까운 벡터이다.

```text
distance가 작다 → 더 유사하다
distance가 크다 → 덜 유사하다
```

### 8. metadata 조건으로 필터링 후 벡터 검색

실무에서는 전체 문서를 대상으로 검색하기보다, 메타데이터 조건으로 검색 범위를 줄인 뒤 유사도 검색을 수행하는 경우가 많다.

```sql
SELECT
    id,
    content,
    JSON_VALUE(metadata, '$.category') AS category,
    JSON_VALUE(metadata, '$.type') AS type,
    VEC_DISTANCE_COSINE(
        embedding,
        VEC_FromText('[0.10, 0.20, 0.32]')
    ) AS distance
FROM vector_store
WHERE JSON_VALUE(metadata, '$.category') = 'spring-ai'
ORDER BY distance
LIMIT 5;
```

### 9. 여러 metadata 조건으로 필터링

```sql
SELECT
    id,
    content,
    JSON_PRETTY(metadata) AS metadata,
    VEC_DISTANCE_COSINE(
        embedding,
        VEC_FromText('[0.10, 0.20, 0.32]')
    ) AS distance
FROM vector_store
WHERE JSON_VALUE(metadata, '$.category') IN ('spring-ai', 'rag')
  AND JSON_VALUE(metadata, '$.type') = 'guide'
  AND JSON_VALUE(metadata, '$.author') = 'kim'
ORDER BY distance
LIMIT 5;
```

### 10. Spring AI 코드에서 metadata filter 사용 예

SQL을 직접 작성하지 않고 Spring AI의 `VectorStore`를 사용하면 `filterExpression`으로 메타데이터 필터링을 적용할 수 있다.

```java
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("Spring AI에서 RAG를 어떻게 구현하나요?")
        .topK(5)
        .filterExpression("category in ['spring-ai', 'rag'] && type == 'guide'")
        .build()
);
```

Spring AI는 이 metadata filter expression을 MariaDB의 JSON path 조건으로 변환하여 검색하며 실무에서는 사실상 아래 4개만 거의 사용한다.
```java
category == 'shopping'

category in ['spring-ai', 'rag']

category == 'shopping' && author == 'kim'

category == 'shopping' || category == 'order'
```
특히 in 과 && 조합이 가장 많이 사용된다. 예를 들면:
```java
.filterExpression(
    "tenantId == 'company-a' && category in ['faq', 'manual']"
)
```
### 11. 전체 흐름

```text
문서 저장
→ content와 metadata 준비
→ content를 임베딩 벡터로 변환
→ content, metadata, embedding 저장

사용자 질문
→ 질문을 임베딩 벡터로 변환
→ metadata 조건으로 검색 범위 제한
→ 벡터 거리 계산
→ 가까운 문서 순서대로 조회
→ 검색 결과를 AI 모델 프롬프트에 함께 전달
```