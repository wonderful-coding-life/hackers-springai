# 벡터 데이터베이스 종류

벡터 데이터베이스는 크게 다음 두 계열로 나뉜다.

1. 전용(Vector-native) 벡터 데이터베이스
2. 관계형 데이터베이스 + 벡터 인덱스 확장

---

## 1. 전용(Vector-native) 벡터 데이터베이스

벡터 검색을 위해 처음부터 설계된 데이터베이스로 대규모 벡터 검색 성능이 우수하며 수억~수십억 벡터 규모까지 확장 가능하다.

- Pinecone
- Milvus
- Qdrant
- Weaviate
- Chroma

---

## 2. 관계형 DB + 벡터 인덱스 확장

기존 관계형 데이터베이스에 벡터 검색 기능을 추가한 방식으로 하나의 관계형 데이터베이스로 애플리케이션을 운영할 수 있는 장점이 있으며 일반적인 RAG 시스템 및 중소~중대 규모 서비스에 적합하다.

- PostgreSQL + pgvector
- MariaDB Vector
- Oracle AI Vector Search

# 🐳 MariaDB Docker 실행 가이드

프로젝트 실행 전에 MariaDB 컨테이너를 먼저 실행해야 합니다.

---

## 1. MariaDB 컨테이너 실행

```bash
docker run -d \
  --name mariadb \
  -p 3306:3306 \
  -e MARIADB_ROOT_PASSWORD=rootpass \
  -e MARIADB_DATABASE=mydb \
  -e MARIADB_USER=myuser \
  -e MARIADB_PASSWORD=mypass \
  mariadb
```
```cmd
docker run -d --name mariadb -p 3306:3306 -e MARIADB_ROOT_PASSWORD=rootpass -e MARIADB_DATABASE=mydb -e MARIADB_USER=myuser -e MARIADB_PASSWORD=mypass mariadb
```
## 2. Spring Boot DB 설정
프로젝트는 아래 설정을 기준으로 MariaDB에 연결됩니다.

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/mydb
spring.datasource.username=myuser
spring.datasource.password=mypass
```

## 3. 컨테이너 상태 확인

```bash
docker ps
```
# ⚠️ 참고
포트 충돌 시
```bash
-p 3307:3306
```
데이터 영속화 (권장)
```bash
docker run -d \
  --name mariadb \
  -p 3306:3306 \
  -v mariadb_data:/var/lib/mysql \
  -e MARIADB_ROOT_PASSWORD=rootpass \
  -e MARIADB_DATABASE=mydb \
  -e MARIADB_USER=myuser \
  -e MARIADB_PASSWORD=mypass \
  mariadb
```

## 4. Maria DB JSON 쿼리 예제
JSON 형태의 데이터를 가지고 있는 컬럼에서 특정한 JSON property의 값을 사용하여 쿼리할 수 있다.
```sql
SELECT * FROM vector_store WHERE JSON_VALUE(metadata, '$.article') = 'ai'
```
숫자 값으로 조회
```sql
SELECT * FROM vector_store WHERE JSON_VALUE(metadata, '$.page_number') = 5
```

문자열 LIKE 검색
```sql
SELECT * FROM vector_store 
WHERE JSON_VALUE(metadata, '$.file_name') LIKE '%인공지능%'
```

여러 조건 조합
```sql
SELECT * FROM vector_store
WHERE JSON_VALUE(metadata, '$.article') = 'ai'
  AND JSON_VALUE(metadata, '$.page_number') = 5
```

IN 조건 사용
```sql
SELECT * FROM vector_store
WHERE JSON_VALUE(metadata, '$.article') IN ('ai', 'ml')
```

JSON 값 존재 여부 확인
```sql
SELECT * FROM vector_store
WHERE JSON_EXISTS(metadata, '$.article')
```

NULL 여부 확인
```sql
SELECT * FROM vector_store
WHERE JSON_VALUE(metadata, '$.article') IS NULL
```

정렬 (ORDER BY)
```sql
SELECT * FROM vector_store
ORDER BY JSON_VALUE(metadata, '$.page_number') DESC
```

특정 필드만 조회 (Projection)
```sql
SELECT 
    JSON_VALUE(metadata, '$.file_name') AS file_name,
    JSON_VALUE(metadata, '$.page_number') AS page_number
FROM vector_store
```

JSON 값 수정 (UPDATE)
```sql
UPDATE vector_store
SET metadata = JSON_SET(metadata, '$.article', 'ml')
WHERE id = 1
```