package com.example.demo.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@Slf4j
public class DateTimeTool {
    @McpTool(
            name = "get-current-datetime",
            title = "현재 날짜 및 시간 조회",
            description = "현재 시스템의 날짜, 시간, 시간대를 조회합니다."
    )
    public DateTimeInfo getCurrentDateTime() {

        ZonedDateTime now = ZonedDateTime.now();
        log.info("get-current-datetime returns {}", now);

        return new DateTimeInfo(
                now.toLocalDate().toString(),
                now.toLocalTime().toString(),
                now.getZone().toString(),
                now.toString()
        );
    }

    public record DateTimeInfo(
            String date,
            String time,
            String timezone,
            String datetime
    ) {}
}
