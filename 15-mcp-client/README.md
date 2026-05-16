# 프로젝트 셋업 (스프링 이니셜라이저)
- 의존성 추가
    - OpenAI
    - Model Context Protocol Client - STDIO 방식과 HTTP 방식 모두 사용 가능
    - Spring Web
    - Lombok
- 애플리케이션 설정 (application.yaml)
  mcp-client는 stdio, streamable-http, sse 세가지를 지원하며, streamable-http로 하면 서버 설정에 따라 streamable, stateless가 결정된다.
```properties
server.port=8088
spring.application.name=demo
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.mcp.client.streamable-http.connections.product-order-server.url=http://localhost:8080
```
# 컨트롤러 구현
- 의존성 주입
    - 기존: private final ProductOrderTool productOrderTool;
    - 변경: private final SyncMcpToolCallbackProvider toolCallbackProvider;
- 프롬프트 옵션 구성
    - 기존:
```java
ToolCallback[] tools = ToolCallbacks.from(productOrderTool);
ChatOptions chatOptions = ToolCallingChatOptions.builder()
        .toolCallbacks(tools)
        .build();
```
- 변경: ToolCallback[] mcpTools = toolCallbackProvider.getToolCallbacks();
```java
ToolCallback[] mcpTools = toolCallbackProvider.getToolCallbacks();
ChatOptions chatOptions = ToolCallingChatOptions.builder()
        .toolCallbacks(mcpTools)
        .build();
```
