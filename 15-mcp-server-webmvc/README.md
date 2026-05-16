# 프로젝트 셋업
- 스프링 이니셜라이저
    - Spring Web
    - Model Context Protocol Server
    - Spring Data JPA
    - H2 Database
    - Lombok


- 애플리케이션 설정 (application.properties)
```properties
spring.application.name=demo
spring.ai.mcp.server.protocol=stateless
```

- 서버 프로토콜 종류에는 stateless(일반적으로 많이 사용)와 streamable 두가지 설정 가능하며, stateless는 tool 실행이 “단방향 요청 → 단일 응답”으로 제한되고, streamable은 tool 실행 중에도 MCP client(AI agent)와 “대화(양방향 상호작용)”가 가능
```
AI → findUser("김민수")
→ 서버: "동명이인이 많습니다. 부서를 알려주세요"
→ AI/사용자: "개발팀"
→ 서버: 조회 계속
→ 결과 반환
```

# MCP Tool 구현 (ProductOrderTool)
```java
// name → 실제 MCP 프로토콜에서 사용하는 식별자 (kebab-case를 사용하면 LLM이 get product orders와 같이 토큰을 자연스럽게 분리할 수 있다)
// title → UI(사람이 보는 화면)용 표시 이름
// description → AI에게 언제 이 tool을 써야 하는지 설명, LLM이 tool을 선택할 때 가장 중요하게 참고하는 필드
@McpTool(name="get-product-orders", title="상품 주문 목록을 조회합니다", description="상품 주문 목록을 조회합니다")
public String getProductOrders() {
}
```
```java
@McpTool(name="cancel-product-order", title="상품 주문을 취소합니다.", description = "특정 상품 주문을 취소할 때 사용합니다")
String cancelProductOrder(@McpToolParam(description="주문번호") String orderNumber) {
}
```

# MCP Server 테스트
- IntelliJ의 HTTP 또는 Postman 등으로 테스트할 수도 있으나 MCP Inspector 권장.
MCP Inspector는 node.js 기반으로 MCP 서버 테스트를 위한 UI를 제공함.
```cmd
npx @modelcontextprotocol/inspector
```