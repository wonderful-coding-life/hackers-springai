## STDIO 기반 MCP Server 설정

STDIO 방식은 별도의 HTTP 서버를 실행하지 않고 표준 입력(stdin)과 표준 출력(stdout)을 통해 MCP Client와 통신합니다.

```
MCP Client
     │
 stdin/stdout
     │
Spring AI MCP Server
```

예제에서는 Spring AI 기반의 MCP Server를 STDIO 방식으로 구성하였습니다.

```properties
spring.ai.mcp.server.stdio=true
```

### MCP Server 정보

```properties
spring.ai.mcp.server.name=demo
spring.ai.mcp.server.version=1.0.0
```

### 콘솔 로그 출력 비활성화
STDIO 기반 MCP Server에서는 stdout이 MCP JSON-RPC 메시지 전송에 사용되므로 로그가 출력되면 프로토콜이 손상될 수 있습니다.
따라서 콘솔 로그 출력을 비활성화 하고 대신 파일로 로그를 출력한다.
```properties
# STDIO 기반 MCP 서버는 stdout으로 JSON 메시지를 주고받기 때문에 콘솔 로그를 비활성화하는 것이 중요합니다.
# 로그는 파일로 남기는 것이 일반적입니다.
# MCP STDIO 프로토콜과 로그가 섞이지 않도록 콘솔 로그를 비활성화합니다.
logging.pattern.console=
# 애플리케이션 로그를 파일로 저장합니다.
logging.file.name=D:/hackers/workspace/datetime-mcp-server.log

```

## MCP Server 테스트
- bootJar로 빌드하고 빌드된 jar 파일을 D:\hackers\workspace\datetime-mcp-server.jar 파일로 복사
- npx @modelcontextprotocol/inspector를 실행하고 다음과 같이 입력
  - Transport Type: STDIO
  - Command: java
  - Arguments: -jar D:\hackers\workspace\datetime-mcp-server.jar
