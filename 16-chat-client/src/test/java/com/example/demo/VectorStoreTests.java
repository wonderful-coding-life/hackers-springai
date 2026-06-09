package com.example.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

@SpringBootTest
public class VectorStoreTests {
    private static final Logger log = LoggerFactory.getLogger(VectorStoreTests.class);

    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private VectorStore vectorStore;

    @Test
    public void testPdfReader() throws IOException {
        // 해커스캠퍼스 온라인 쇼핑몰 반품 정책 매뉴얼.pdf
        // 해커스캠퍼스 온라인 쇼핑몰 반품 FAQ.pdf
        DocumentReader reader = new PagePdfDocumentReader("classpath:/해커스캠퍼스 온라인 쇼핑몰 반품 정책 매뉴얼.pdf");
        List<Document> documents = reader.read();
        documents.forEach(document -> document.getMetadata().put("category", "shopping"));
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        vectorStore.write(splitter.split(documents));
    }

    @Test
    public void testSimilaritySearchInPdfFile() {
        String question1 = "제가 교재를 구매했는데 책에 필기를 조금 했습니다. 반품하려면 배송비는 누가 부담하고 환불은 받을 수 있나요?";
        String question2 = "쿠폰과 적립금을 사용해서 결제했는데 일부 상품만 반품하면 환불 금액은 어떻게 계산되나요?";
        String question3 = "주문한 상품과 다른 상품이 배송됐는데 반품 절차와 환불까지 걸리는 시간을 알려주세요.";

        String question = question1;
        var request = SearchRequest.builder()
                .query(question)
                .topK(3)
                .filterExpression("category == 'shopping'")
                .build();

        var documents = vectorStore.similaritySearch(request);
        var information = String.join("\n", documents.stream().map(Document::getText).toList());
        var prompt = MessageFormat.format("""
                당신은 해커스캠퍼스 쇼핑몰 고객센터 상담원이야.
                친절하고 명확하며 간략하게 답변 해 줘.
                
                [정보]
                {0}
                [질문]
                {1}
                """, information, question);

        var completion = chatModel.call(prompt);
        log.info("\n{}", completion);
    }
}
