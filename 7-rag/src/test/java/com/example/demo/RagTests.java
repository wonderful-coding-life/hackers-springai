package com.example.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.reader.TextReader;
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
public class RagTests {
    private static final Logger log = LoggerFactory.getLogger(RagTests.class);

    @Autowired
    private OpenAiChatModel chatModel;

    @Autowired
    private OpenAiEmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;

    private final List<String> texts = List.of("""
            [무선 청소기 사용 설명서]
            제품 사용 전 반드시 배터리를 완전히 충전해 주세요.
            전원 버튼을 3초간 길게 눌러 전원을 켜면 LED 표시등이 점등됩니다.
            먼지통이 가득 차면 흡입력이 떨어질 수 있으므로, 청소 후 반드시 먼지통과 필터를 세척하세요.
            필터는 완전히 건조된 후 다시 장착해야 하며, 젖은 상태로 사용할 경우 고장의 원인이 될 수 있습니다.
            배터리는 약 500회 충전이 가능합니다. 장시간 사용하지 않을 때는 완충 후 서늘한 곳에 보관하세요.
            """, """
            [전자제품 A/S 및 교환 정책]
            본 제품은 구입일로부터 1년간 무상 보증이 제공됩니다.
            단, 소비자 과실이나 천재지변으로 인한 손상은 보증 대상에서 제외됩니다.
            제품 수리 시, 서비스 센터 방문 또는 택배 접수가 가능합니다.
            교환 또는 환불은 구입 후 7일 이내, 제품 및 포장 상태가 완전할 경우에만 가능합니다.
            수리 완료 후에는 수리 내역서와 함께 보증기간이 자동으로 연장되지 않으며, 교체된 부품은 별도 보증되지 않습니다.
            """, """
            [기업 내부 인사 규정 요약]
            정규직 직원은 입사 후 3개월의 수습 기간을 거치며, 근무 성과 평가 결과에 따라 정규 전환이 결정됩니다.
            연차휴가는 근속연수에 따라 차등 부여되며, 사용하지 않은 휴가는 익년도 1월 말까지 이월 가능합니다.
            재택근무는 부서장 승인 하에 주 2회까지 허용됩니다.
            사내 교육 프로그램은 분기별로 운영되며, 필수 교육을 미이수할 경우 인사고과에 반영될 수 있습니다.
            퇴사 시에는 최소 30일 전에 인사팀에 서면으로 통보해야 합니다.
            """, """
            [온라인 쇼핑몰 반품 정책]
            고객님께서 구매하신 상품은 수령 후 7일 이내에 반품이 가능합니다.
            단, 포장이 훼손되었거나 사용 흔적이 있는 경우에는 반품이 불가합니다.
            단순 변심으로 인한 반품의 경우 왕복 배송비가 청구될 수 있습니다.
            제품 이상이나 오배송의 경우 모든 배송비는 당사에서 부담합니다.
            환불은 반품 확인 후 3영업일 이내에 결제수단으로 처리됩니다.
            """, """
            [기업 개인정보 처리방침]
            당사는 서비스 제공을 위해 최소한의 개인정보만을 수집하며,
            수집된 정보는 회원 관리 및 서비스 품질 개선에만 사용됩니다.
            고객은 언제든지 자신의 개인정보 열람, 수정, 삭제를 요청할 수 있습니다.
            수집된 정보는 법적 보관 기간이 지난 후 즉시 파기됩니다.
            당사는 개인정보 보호를 위해 암호화 저장 및 접근 권한 관리를 시행합니다.
            """, """
            [사내 보안 정책 안내]
            모든 직원은 회사 네트워크에 접속할 때 사내 VPN을 사용해야 합니다.
            외부 저장장치(USB, 외장하드 등)의 무단 연결은 금지됩니다.
            업무용 PC는 자동 잠금 설정을 10분 이내로 유지해야 하며,
            비밀번호는 90일마다 변경해야 합니다.
            보안 위반 행위 적발 시 인사위원회 심의를 통해 징계 조치가 이루어질 수 있습니다.
            """, """
            [출장비 및 경비 정산 규정]
            출장비는 교통비, 숙박비, 식비, 일비로 구분됩니다.
            모든 경비는 영수증 첨부를 원칙으로 하며, 제출 기한은 출장 종료 후 7일 이내입니다.
            사전 승인 없이 발생한 경비는 정산 대상에서 제외될 수 있습니다.
            법인카드 사용 내역은 자동으로 ERP 시스템에 반영됩니다.
            부적절한 경비 사용이 확인될 경우, 차기 급여에서 공제될 수 있습니다.
            """);

    @Test
    public void testEmbedding() {
        float[] embedding = embeddingModel.embed("""
            [무선 청소기 사용 설명서]
            제품 사용 전 반드시 배터리를 완전히 충전해 주세요.
            전원 버튼을 3초간 길게 눌러 전원을 켜면 LED 표시등이 점등됩니다.
            먼지통이 가득 차면 흡입력이 떨어질 수 있으므로, 청소 후 반드시 먼지통과 필터를 세척하세요.
            필터는 완전히 건조된 후 다시 장착해야 하며, 젖은 상태로 사용할 경우 고장의 원인이 될 수 있습니다.
            배터리는 약 500회 충전이 가능합니다. 장시간 사용하지 않을 때는 완충 후 서늘한 곳에 보관하세요.
            """);
        log.info("\n{} {}", embedding.length, embedding);
    }

    @Test
    public void testVectorStore() {
        var documents = texts.stream().map(Document::new).toList();
        vectorStore.write(documents);
    }

    @Test
    public void testSimilaritySearch() {
        var question1 = "무선청소기 배터리 수명은 얼마나 되나요?";
        var question2 = "사용자의 실수로 제품이 고장 났을 때 무상 수리가 가능한가요?";
        var question3 = "신입사원은 수습 기간이 얼마나 되나요?";

        var question = question2;
        var documents = vectorStore.similaritySearch(question);
        var information = String.join("\n", documents.stream().map(Document::getText).toList());
        var prompt = MessageFormat.format("""
                다음의 정보를 바탕으로 답변을 해 줘.
                [정보]
                {0}
                [질문]
                {1}
                """, information, question);
        log.info("\n{}", prompt);

        var completion = chatModel.call(prompt);
        log.info("\n{}", completion);
    }

    @Test
    public void testTextReader() {
        var reader = new TextReader("classpath:/운수좋은날.txt");
        var documents = reader.read();
        var splitter = TokenTextSplitter.builder().withChunkSize(300).build();
        vectorStore.write(splitter.split(documents));
    }

    @Test
    public void testSimilaritySearchInTextFile() {
        String question = "주인공 김첨지는 어떤 일을 하는 사람인가요?";
//        String question = "김첨지 아내는 무슨 병에 걸렸나요?";
//        String question = "김첨지가 '오늘은 운수 좋은 날'이라고 생각한 이유는 무엇인가요?";
//        String question = "김첨지는 왜 비 오는 날에 나가 일하게 되었나요?";

        var request = SearchRequest.builder()
                .query(question)
                .topK(3)
                .filterExpression("source == '운수좋은날.txt'")
                .build();

        var documents = vectorStore.similaritySearch(request);
        var information = String.join("\n", documents.stream().map(Document::getText).toList());
        var prompt = MessageFormat.format("""
                다음의 정보를 기반으로 하여 답을 하고, 정보가 없는 경우에는 모른다고 답변 하세요.
                [정보]
                {0}
                [질문]
                {1}
                """, information, question);

        var completion = chatModel.call(prompt);
        log.info("\n{}", completion);
    }

    @Test
    public void testPdfReader() throws IOException {
        DocumentReader reader = new PagePdfDocumentReader("classpath:/인공지능_시대의_예술.pdf");
        List<Document> documents = reader.read();
        documents.forEach(document -> document.getMetadata().put("article", "ai"));
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        vectorStore.write(splitter.split(documents));
    }

    @Test
    public void testSimilaritySearchInPdfFile() {
        String question1 = "인공지능 시대의 예술에 대해 제시된 5가지 시나리오는 무엇이며 각각의 특징은 무엇인가?";
        String question2 = "인공지능 기술 발전이 예술성과 창의성의 개념에 어떤 변화를 가져올 것으로 예상되는가?";
        String question3 = "인공지능 시대의 예술 변화에 대응하기 위해 교육, 기술, 정책 측면에서 어떤 전략이 제시되었는가?";

        String question = question2;

        var request = SearchRequest.builder()
                .query(question)
                .topK(3)
                .filterExpression("article == 'ai'")
                .build();

        var documents = vectorStore.similaritySearch(request);
        var information = String.join("\n", documents.stream().map(Document::getText).toList());
        var prompt = MessageFormat.format("""
                다음의 정보를 기반으로 하여 답을 하고, 정보가 없는 경우에는 모른다고 답변 하세요.
                [정보]
                {0}
                [질문]
                {1}
                """, information, question);

        var completion = chatModel.call(prompt);
        log.info("\n{}", completion);
    }
}
