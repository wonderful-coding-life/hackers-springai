
# Chat Client 프로젝트

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

spring.ai.mcp.client.streamable-http.connections.product-order-server.url=http://localhost:8090
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

* FAQ 검색은 항상 수행하지 않고 LLM이 필요하다고 판단한 경우에만 수행하도록 구현합니다.

```text
기존 방식
QuestionAnswerAdvisor
→ 매 요청마다 RAG 수행

개선 방식
faqSearchTool
→ LLM이 필요할 때만 벡터 검색 수행
```

## 질문 유형
- 단순 변심도 반품이 되나요?
- 제가 교재를 구매했는데 책에 필기를 조금 했습니다. 반품하려면 배송비는 누가 부담하고 환불은 받을 수 있나요?
- 쿠폰과 적립금을 사용해서 결제했는데 일부 상품만 반품하면 환불 금액은 어떻게 계산되나요?
- 주문한 상품과 다른 상품이 배송됐는데 반품 절차와 환불까지 걸리는 시간을 알려주세요.
- 주문 목록을 알려 주세요.

