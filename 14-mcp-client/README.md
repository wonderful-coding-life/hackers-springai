# 핵심 키워드
- Model Context Protocol Client
- spring.ai.mcp.client.streamable-http.connections.product-order-server.url=http://localhost:8080
- @Autowired SyncMcpToolCallbackProvider toolCallbackProvider
- ToolCallback[] mcpTools = toolCallbackProvider.getToolCallbacks();

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
    - 변경: private final ToolCallbackProvider toolCallbackProvider;
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

# 보안 - 예제 아직 미구현

## 서버 공통 토큰
- 내부 시스템에서 많이 사용함
- MCP Client를 전적으로 신뢰
- 사용자 개별 아이디와 같은 정보는 MCP Client가 자체 보안 모듈로 획득한 후 MCP Server로 전달
- spring.ai.mcp.client.streamable-http.connections.product-order-server.headers.Authorization=Bearer xxx

## 사용자별 토큰
- MCP 서버에서 사용자별로 토큰을 발생
- MCP 클라이언트가 사용자로부터 토큰을 받아 MCP 서버와 통신할 때 사용
- 컨트롤러에서 사용자로부터 MCP 서버 토큰을 전달 받음 (@RequestHeader("X-MCP-Token") String token)
```java
@PostMapping("/chat")
public String chat(
        @RequestHeader("X-MCP-Token") String mcpToken,
        @RequestBody String message
) {
    try {
        tokenHolder.setCurrentUserMcpToken(mcpToken);

        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    } finally {
        tokenHolder.clear();
    }
}
```
- 전달받은 토큰을 LocalThread<String>() holder에 저장
```java
@Component
public class McpTokenHolder {

    private static final ThreadLocal<String> holder
            = new ThreadLocal<>();

    public void setCurrentUserMcpToken(String token) {
        holder.set(token);
    }

    public String getCurrentUserMcpToken() {
        return holder.get();
    }

    public void clear() {
        holder.remove();
    }
}
```
- WebClient를 커스터마이즈해서 holder에 있는 MCP Server용 토큰을 filter로 구현
```java
@Bean
WebClientCustomizer mcpWebClientCustomizer(McpTokenHolder tokenHolder) {
  return builder -> builder.filter((request, next) -> {

    String token = tokenHolder.getCurrentUserMcpToken();

    ClientRequest newRequest = ClientRequest.from(request)
            .headers(headers -> {
              if (token != null) {
                headers.setBearerAuth(token);
              }
            })
            .build();

    return next.exchange(newRequest);
  });
}
```

- MCP Server 쪽에서는 일반 Spring Security Resource Server처럼 /mcp 엔드포인트를 JWT 인증 대상으로 보호하면 됩니다.
```groovy
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
```

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/mcp/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .build();
    }
}
```

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          public-key-location: classpath:public.pem
```

- 내부 사용자 정보가 필요하면 MCP Tool에서
```java
@Tool(description = "현재 사용자의 주문 목록을 조회합니다.")
public List<Order> getMyOrders() {
    Jwt jwt = (Jwt) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();

    String userId = jwt.getSubject();

    return orderService.findByUserId(userId);
}
```