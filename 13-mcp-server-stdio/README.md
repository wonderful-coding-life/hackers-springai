## STDIO 기반 MCP Server 설정

예제에서는 Spring AI 기반의 MCP Server를 STDIO 방식으로 구성하였습니다.

```properties
spring.application.name=demo

spring.ai.mcp.server.name=demo
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.stdio=true

spring.main.banner-mode=off
spring.main.log-startup-info=false
logging.level.root=OFF
```

### MCP Server 정보

```properties
spring.ai.mcp.server.name=demo
spring.ai.mcp.server.version=1.0.0
```

MCP Client가 서버에 연결할 때 사용되는 MCP Server의 이름과 버전을 설정합니다.

### STDIO 방식 활성화

```properties
spring.ai.mcp.server.stdio=true
```

STDIO(Standard Input/Output) 기반 MCP Server를 활성화합니다.

STDIO 방식은 별도의 HTTP 서버를 실행하지 않고 표준 입력(stdin)과 표준 출력(stdout)을 통해 MCP Client와 통신합니다.

```
MCP Client
     │
 stdin/stdout
     │
Spring AI MCP Server
```

### 배너 출력 비활성화

```properties
spring.main.banner-mode=off
```

Spring Boot 시작 시 출력되는 배너를 비활성화합니다.

STDIO 방식에서는 stdout이 MCP 프로토콜 통신에 사용되므로 불필요한 출력은 제거하는 것이 좋습니다.

### 시작 로그 비활성화

```properties
spring.main.log-startup-info=false
```

Spring Boot 시작 시 출력되는 환경 정보 및 시작 로그를 비활성화합니다.

### 로그 출력 비활성화

```properties
logging.level.root=OFF
```

모든 로그 출력을 비활성화합니다.

STDIO 기반 MCP Server에서는 stdout이 MCP JSON-RPC 메시지 전송에 사용되므로 로그가 출력되면 프로토콜이 손상될 수 있습니다.

> 주의: STDIO 기반 MCP Server에서는 `System.out.println()` 사용을 피해야 합니다. stdout에는 MCP 프로토콜 메시지만 출력되어야 합니다.

## MCP Server 테스트
- bootJar로 빌드하고 빌드된 jar 파일을 D:\hackers\workspace\datetime-mcp-server.jar 파일로 복사
- npx @modelcontextprotocol/inspector를 실행하고 다음과 같이 입력
  - Transport Type: STDIO
  - Command: java
  - Arguments: -jar D:\hackers\workspace\datetime-mcp-server.jar
