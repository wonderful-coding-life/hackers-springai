#!/usr/bin/env node

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
    CallToolRequestSchema,
    ListToolsRequestSchema
} from "@modelcontextprotocol/sdk/types.js";

const server = new Server(
    {
        name: "datetime-mcp-server",
        version: "1.0.0"
    },
    {
        capabilities: {
            tools: {}
        }
    }
);

server.setRequestHandler(
    ListToolsRequestSchema,
    async () => ({
        tools: [
            {
                name: "current_datetime",
                description: "현재 시스템 날짜와 시간을 반환합니다.",
                inputSchema: {
                    type: "object",
                    properties: {}
                }
            }
        ]
    })
);

server.setRequestHandler(
    CallToolRequestSchema,
    async (request) => {

        if (request.params.name === "current_datetime") {

            const now = new Date();

            return {
                content: [
                    {
                        type: "text",
                        text: JSON.stringify({
                            iso: now.toISOString(),
                            local: now.toLocaleString(),
                            timestamp: now.getTime()
                        }, null, 2)
                    }
                ]
            };
        }

        throw new Error(
            `Unknown tool: ${request.params.name}`
        );
    }
);

const transport = new StdioServerTransport();

await server.connect(transport);

/*
 * 중요
 * stdout 사용 금지
 * console.log 사용 금지
 */
console.error(
    "Datetime MCP Server started"
);

