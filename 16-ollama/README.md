## 프로젝트 생성

다음 의존성을 포함하여 프로젝트를 생성합니다.

* Spring Web
* Ollama
* Model Context Protocol Client
* MariaDB Vector Database
* PDF Document Reader
* Spring Data JPA
* MariaDB Driver
* Lombok
* Spring Security

application.properties

```properties
spring.application.name=demo

#logging.level.org.springframework.ai=DEBUG
#logging.level.org.springframework.ai.chat.client.advisor=TRACE
#logging.level.org.springframework.ai.tool=DEBUG
#logging.level.org.springframework.ai.mcp=DEBUG

spring.datasource.url=jdbc:mariadb://localhost:3306/mydb
spring.datasource.username=myuser
spring.datasource.password=mypass

spring.ai.vectorstore.mariadb.initialize-schema=true
spring.ai.chat.memory.repository.jdbc.initialize-schema=always
spring.jpa.hibernate.ddl-auto=update

spring.ai.ollama.chat.model=gpt-oss:20b
spring.ai.ollama.embedding.model=qwen3-embedding:8b

spring.ai.mcp.client.streamable-http.connections.product-order-server.url=http://localhost:8090
```
---

## Ollama 다운로드 및 실행

Ollama 홈페이지(https://ollama.com)에 접속하여 다운로드 및 설치
아래는 윈도우 파워쉘에서 설치하는 커맨드입니다.

```shell
irm https://ollama.com/install.ps1 | iex
```

Ollama 모델 다운로드 및 실행

```bash
ollama pull gpt-oss
ollama run gpt-oss
```

---

## 벡터 데이터베이스 준비

캠퍼스 온라인 쇼핑몰 반품 FAQ 및 반품 정책 매뉴얼을 임베딩합니다.

자세한 내용은 `VectorStoreTests`를 참고합니다.

---

## 챗 클라이언트 구성

다음 기능을 구현합니다.

* 대화 이력 유지를 위해 `MessageChatMemoryAdvisor`를 추가합니다.
* FAQ 검색 도구(`faqSearchTool`)를 추가합니다.
    * `QuestionAnswerAdvisor`는 모든 요청마다 RAG 검색을 수행하므로 사용하지 않습니다.
    * 대신 LLM이 필요하다고 판단한 경우에만 `faqSearchTool`을 호출하여 벡터 검색을 수행합니다.
* 주문 조회 및 주문 취소 MCP Server를 도구로 등록합니다.
* 인증된 사용자 이름을 Tool Context로 전달합니다.
* `Flux<String>`을 사용하여 응답을 실시간으로 스트리밍합니다.

---

## 주의 사항

* MCP Server에서 제공하는 도구가 너무 많으면 응답 생성 속도가 느려질 수 있습니다.
* 필요한 도구만 선택적으로 등록하는 것이 좋습니다.
* FAQ 검색은 항상 수행하지 않고 LLM이 필요하다고 판단한 경우에만 수행하도록 구현합니다.

특히 이번 예제의 핵심 포인트는

```text
기존 방식
QuestionAnswerAdvisor
→ 매 요청마다 RAG 수행

개선 방식
faqSearchTool
→ LLM이 필요할 때만 벡터 검색 수행
```


## Ollama 다운로드 및 실행

```bash
ollama pull gpt-oss
ollama run gpt-oss
```

---

## 프로젝트 생성

다음의 의존성을 포함하여 프로젝트를 생성합니다.

* Spring Web
* Ollama
* Model Context Protocol Client
* MariaDB Vector Database
* PDF Document Reader
* Spring Data JPA
* MariaDB Driver
* Lombok
* Spring Security

## 벡터데이터베이스 준비

* 캠퍼스 온라인 쇼핑몰 반품 FAQ, 정책 메뉴얼을 임베딩 (VectorStoreTests 참조)

## 챗 클라이언트 구성

* MessageChatMemoryAdvisor 추가
* QuestionAnswerAdvisor 추가하면 매번 RAG 검색을 하므로 대신 faqSearchTool을 사용하여 LLM이 판단하도록 합니다.
* 주문 목록 조회, 주문 취소 MCP Server를 도구로 추가하고 사용자 이름을 도구 컨텍스트로 전달
* Flux<String>으로 실시간 토큰 전송

## Ollama 다운로드 및 실행

```shell
ollama pull gpt-oss
ollama run gpt-oss
```

## 주의 사항
* 도구(Tool)이 너무 많이 사용되면 gpt-oss가 정상적으로 동작하지 않을 수 있다.