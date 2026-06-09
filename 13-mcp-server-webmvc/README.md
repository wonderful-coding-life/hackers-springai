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
spring.ai.mcp.server.protocol=streamable
```

# MCP Tool 구현 (ProductOrderTool)
기존 도구(Tool)에서 사용하던 애노테이션만 다음과 같이 바꾸면 된다.
```text
@Tool(description = "상품 주문 목록을 알려줍니다")
--> @McpTool(name="get-product-orders", title = "상품 주문 조회", description="상품 주문 목록을 조회합니다")

@ToolParam(description = "주문번호") String orderNumber
--> @McpToolParam(description="주문번호") String orderNumber

ToolContext toolContext
String username = (String) toolContext.getContext().get("username");

--> McpMeta mcpMeta
--> String username = (String) mcpMeta.get("username");
```

```java
// name → 실제 MCP 프로토콜에서 사용하는 식별자 (kebab-case를 사용하면 LLM이 get product orders와 같이 토큰을 자연스럽게 분리할 수 있다)
// title → UI(사람이 보는 화면)용 표시 이름
// description → AI에게 언제 이 tool을 써야 하는지 설명, LLM이 tool을 선택할 때 가장 중요하게 참고하는 필드
@McpTool(name="get-product-orders", title="상품 주문 목록을 조회합니다", description="상품 주문 목록을 조회합니다")
public String getProductOrders() {
}
```
```java
@McpTool(name="cancel-product-order", title = "상품 주문 취소", description = "특정 상품 주문을 취소할 때 사용합니다")
String cancelProductOrder(@McpToolParam(description="주문번호") String orderNumber, McpMeta mcpMeta) {
    String username = (String) mcpMeta.get("username");
}
```

# MCP Server 테스트
- IntelliJ의 HTTP 또는 Postman 등으로 테스트할 수도 있으나 MCP Inspector 권장.
MCP Inspector는 node.js 기반으로 MCP 서버 테스트를 위한 UI를 제공함.
```cmd
npx @modelcontextprotocol/inspector
```

```text
Transport Type: Streamable HTTP
URL: http://localhost:8080/mcp
Connection Type: Via Proxy
```