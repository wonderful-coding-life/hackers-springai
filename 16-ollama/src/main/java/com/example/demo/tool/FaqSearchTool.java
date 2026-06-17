package com.example.demo.tool;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FaqSearchTool {

    @Autowired
    private VectorStore vectorStore;

    @Tool(description = "캠퍼스 온라인 쇼핑몰 반품 FAQ와 반품 정책 매뉴얼에서 관련 내용을 검색합니다.")
    public String searchFaq(
            @ToolParam(description = "사용자 질문 또는 검색어")
            String query
    ) {
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3)
                        .similarityThreshold(0.7)
                        .build()
        );

        if (documents.isEmpty()) {
            return "관련 FAQ 또는 반품 정책 내용을 찾지 못했습니다.";
        }

        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}
