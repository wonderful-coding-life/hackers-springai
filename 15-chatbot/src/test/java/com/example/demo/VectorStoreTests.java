package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class VectorStoreTests {
    @Autowired
    private VectorStore vectorStore;

    @Test
    public void testPdfReader() throws IOException {
        DocumentReader reader = new PagePdfDocumentReader("classpath:/해커스캠퍼스 온라인 쇼핑몰 반품 정책 매뉴얼.pdf");
        embedPdfDocument("classpath:/해커스캠퍼스 온라인 쇼핑몰 반품 정책 매뉴얼.pdf");
        embedPdfDocument("classpath:/해커스캠퍼스 온라인 쇼핑몰 반품 FAQ.pdf");
    }

    private void embedPdfDocument(String path) {
        DocumentReader reader = new PagePdfDocumentReader(path);
        List<Document> documents = reader.read();
        documents.forEach(document -> document.getMetadata().put("category", "shopping"));
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        vectorStore.write(splitter.split(documents));
    }
}
