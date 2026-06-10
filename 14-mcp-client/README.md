# 핵심 키워드
```
- Model Context Protocol Client
- Streamable HTTP 방식 MCP 서버 주소
  spring.ai.mcp.client.streamable-http.connections.xxx.url=xxx
- STDIO 방식 MCP 서버 실행 명령어
  spring.ai.mcp.client.stdio.connections.xxx.command=xxx
  실행 시 전달할 명령행 인수
  spring.ai.mcp.client.stdio.connections.xxx.args=xxx
  MCP 서버에 전달할 환경 변수(API Key 등)
  spring.ai.mcp.client.stdio.connections.xxx.env.xxx=${xxx}
```

# Spring AI 2.0.0-RC1에서는 도구 호출(MCP 호출 포함)은 ChatClient로 해야 한다.
```java
@Autowired
private ChatClient chatClient;

// 설정된 MCP 서버들에서 발견(Discovery)한 Tool들을 ToolCallback 형태로 제공
@Autowired
private ToolCallbackProvider toolCallbackProvider;

@PostMapping("/chats")
public String postChats(@RequestBody String message) {
  return chatClient.prompt()
          .system(systemMessage)
          .user(message)
          .tools(toolCallbackProvider)
          .toolContext(Map.of("username", "user"))
          .call().content();
}
```

# 프로젝트 셋업 (스프링 이니셜라이저)
- 의존성 추가
    - OpenAI
    - Model Context Protocol Client - STDIO 방식과 HTTP 방식 모두 사용 가능
    - Spring Web
    - Lombok
- 애플리케이션 설정 (application.yaml)
```properties
server.port=8088
spring.application.name=demo
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.mcp.client.streamable-http.connections.product-order-server.url=http://localhost:8080
```
