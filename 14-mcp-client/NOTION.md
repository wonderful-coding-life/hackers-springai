# Spring AI + Notion MCP Server 연동 실습

## 개요

GitHub, Notion, Canva 와 같은 많은 서비스들은 STDIO 기반의 MCP(Model Context Protocol) 서버를 제공합니다.
Spring AI 기반의 MCP Client를 사용하면 이러한 서비스들과 손쉽게 연동할 수 있습니다.

이번 예제에서는 Spring AI 애플리케이션에서 Notion MCP Server와 연동하여 다음 작업들을 수행해 봅니다.

* 페이지 생성(Create)
* 페이지 조회(Retrieve)
* 페이지 수정(Update)
* 페이지 삭제(Delete)

---

# Notion 연동 준비

## 1. Notion 토큰 생성 및 페이지 연결

Notion은 다음과 같은 구조를 가집니다.

* 워크스페이스(Workspace)

    * 페이지(Page)

        * 하위 페이지(Page)

따라서 애플리케이션에서 특정 페이지를 제어하려면:

1. 워크스페이스에서 Integration(Token) 생성
2. 연동할 페이지에 해당 토큰 연결
3. 해당 페이지의 `page_id` 확보

예제에서는 다음과 같이 구성합니다.

* 워크스페이스: `해커스캠퍼스`
* 페이지 제목: `스프링 AI 예제`

그리고 이 페이지의 `page_id`를 시스템 메시지에서 사용합니다.

---

# MCP Inspector로 사전 테스트

애플리케이션 연동 전에 MCP Inspector를 사용하여 연결 테스트를 수행합니다.

```bash
npx @modelcontextprotocol/inspector -e NOTION_TOKEN=ntn_xxxxxxxxxxxxx -- npx -y @notionhq/notion-mcp-server
```

> 참고:
> MCP Inspector를 먼저 실행한 뒤 브라우저에서 Notion MCP Server를 등록할 수도 있지만,
> 위와 같이 한 번에 실행하는 방식이 더 편리합니다.

---

# MCP Inspector - API 테스트

## 1. Notion Version

`Notion Version`은 날짜 기반 버전이지만
MCP Server가 내부적으로 자동 처리하므로 비워 둡니다.

---

## 2. Parent 설정

`Parent`에는 JSON 형태로 `page_id`를 전달합니다.

```json
{
  "page_id": "36e50b450ffa80299a51ec265c156558"
}
```

---

## 3. 제목만 포함한 페이지 생성

```json
{
  "title": [
    {
      "text": {
        "content": "Spring AI MCP 실습 결과"
      }
    }
  ]
}
```

---

## 4. children 블록 추가

```json
[
  {
    "object": "block",
    "type": "heading_2",
    "heading_2": {
      "rich_text": [
        {
          "type": "text",
          "text": {
            "content": "MCP Client 실습"
          }
        }
      ]
    }
  },
  {
    "object": "block",
    "type": "paragraph",
    "paragraph": {
      "rich_text": [
        {
          "type": "text",
          "text": {
            "content": "Spring AI에서 STDIO 방식의 Notion MCP Server를 연동하였다."
          }
        }
      ]
    }
  },
  {
    "object": "block",
    "type": "bulleted_list_item",
    "bulleted_list_item": {
      "rich_text": [
        {
          "type": "text",
          "text": {
            "content": "retrieve page 테스트 성공"
          }
        }
      ]
    }
  },
  {
    "object": "block",
    "type": "bulleted_list_item",
    "bulleted_list_item": {
      "rich_text": [
        {
          "type": "text",
          "text": {
            "content": "create page 테스트 성공"
          }
        }
      ]
    }
  },
  {
    "object": "block",
    "type": "bulleted_list_item",
    "bulleted_list_item": {
      "rich_text": [
        {
          "type": "text",
          "text": {
            "content": "append children 테스트 성공"
          }
        }
      ]
    }
  }
]
```

---

# Spring AI MCP Client 설정

## application.properties

```properties
spring.ai.mcp.client.stdio.connections.notion.command=npx.cmd
spring.ai.mcp.client.stdio.connections.notion.args=-y,@notionhq/notion-mcp-server
spring.ai.mcp.client.stdio.connections.notion.env.NOTION_TOKEN=${NOTION_TOKEN}
```

---

## 강의 노트

### 운영체제별 command 차이

* Windows: `npx.cmd`
* macOS / Linux: `npx`

STDIO 기반 방식이므로 운영체제 환경에 맞는 실행 파일을 사용해야 합니다.

---

# 실습 프롬프트 예제

## 1. 내가 접근 가능한 Notion 페이지 조회

```text
내가 볼 수 있는 노션 페이지 목록 알려 줘
```

---

## 2. 새로운 페이지 생성

```text
유럽에서 가장 인구가 많은 나라 세곳에 대해 정리해서 노션 페이지에 추가해 줘
```

---

## 3. 기존 페이지 수정

```text
노션 페이지 중에 "유럽 인구 상위 3개국 요약" 페이지를 찾아
유럽에서 가장 인구가 적은 나라 세곳의 정보도 마지막에 추가해 줘.
```

---

## 4. 페이지 삭제

```text
"유럽 인구 상위 3개국 요약" 페이지를 확인 없이 바로 휴지통으로 이동해 줘.
```
