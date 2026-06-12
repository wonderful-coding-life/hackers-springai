
## 12차시 (stream)

LLM의 응답이 모두 생성된 후 한 번에 반환되는 방식이 아니라, 생성되는 토큰을 실시간으로 전달받아 SSE(Server-Sent Events)를 통해 클라이언트에 전송하는 방법을 학습합니다.

이를 통해 사용자는 AI의 응답을 기다리는 동안 생성 과정을 실시간으로 확인할 수 있으며, 더욱 자연스럽고 향상된 사용자 경험(UX)을 제공할 수 있습니다.

## 13차시 (mcp-server-webmvc, mcp-server-stdio)

8차시에서 작성했던 주문 조회 및 주문 취소 도구(Tool)를 Streamable HTTP 기반 MCP Server로 마이그레이션합니다.

또한 파일 시스템에 접근하여 텍스트 파일을 읽고 쓰는 기능과 현재 시간을 조회하는 기능을 STDIO 기반 MCP Server로 구현합니다.

## 14차시 (mcp-client)

13차시에서 직접 구현한 Streamable HTTP 기반 MCP Server와 STDIO 기반 MCP Server를 Spring AI 애플리케이션에 연동하고 테스트합니다.

또한 Notion MCP Server와 Open-Meteo MCP Server를 추가로 등록하여, Java뿐만 아니라 다양한 언어와 플랫폼으로 구현된 MCP Server도 손쉽게 연동할 수 있음을 확인합니다.

이를 통해 AI가 단순 답변을 넘어 외부 도구를 활용하여 작업을 수행하는 과정을 살펴보며, AI Agent의 기본 개념을 이해합니다.

## 15차시 (chatbot)
Streaming 기반 AI 챗봇을 구현합니다.

Spring AI와 WebFlux를 활용하여 AI 응답을 실시간으로 스트리밍하는 챗봇을 개발하고, 사용자 경험을 향상시키는 방법을 학습합니다.

## 16차시 (ollama)

Ollama를 활용하여 로컬 LLM을 연동합니다.

OpenAI API 없이도 로컬 환경에서 LLM을 실행하고 Spring AI와 연동하여 AI 애플리케이션을 개발하는 방법을 학습합니다.

## Codex CLI 설치
```shell
npm install -g @openai/codex
```