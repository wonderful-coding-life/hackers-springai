package com.example.demo.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

@Component
@Slf4j
public class FileSystemTool {
    // D:/workspace 폴더에 있는 파일 보여줘.
    @McpTool(
            name = "get-directory",
            title = "디렉토리 목록을 조회한다",
            description = "매개변수로 전달된 디렉토리의 파일 목록을 조회한다"
    )
    public List<String> listFiles(
            @McpToolParam(description = "디렉터리") String directory)
            throws IOException {

        try (Stream<Path> stream = Files.list(Path.of(directory))) {
            return stream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
        }
    }

    // D:/hackers/workspace/hello.txt 내용을 알려줘.
    @McpTool(
            name = "read-text-file",
            title = "텍스트 파일을 읽는다",
            description = "지정한 파일 경로의 텍스트 파일을 읽는다."
    )
    public String readTextFile(
            @McpToolParam(description = "파일 경로") String path)
            throws IOException {

        return Files.readString(Path.of(path));
    }

    // D:/hackers/workspace/hello.txt 파일에 "안녕하세요"를 저장해줘.
    @McpTool(
            name = "write-text-file",
            title = "텍스트 파일을 저장한다",
            description = "지정한 경로에 텍스트 파일을 저장하거나 기존 내용을 덮어쓴다."
    )
    public String writeTextFile(
            @McpToolParam(description = "파일 경로") String path,
            @McpToolParam(description = "저장할 텍스트 내용") String content)
            throws IOException {

        Path filePath = Path.of(path);

        // 상위 디렉터리가 없으면 생성
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        Files.writeString(filePath, content);

        return "파일 저장 완료: " + filePath.toAbsolutePath();
    }

    // 현재 운영체제가 뭐야?
    @McpTool(
            name = "get-os-information",
            title = "운영체제 정보를 조회한다.",
            description = "운영체제 정보를 조회한다."
    )
    public String getOsInfo() {
        return System.getProperty("os.name")
                + " "
                + System.getProperty("os.version");
    }

    // 지금 몇시야
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
