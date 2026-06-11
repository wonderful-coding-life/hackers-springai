package com.example.demo.controller;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Controller
public class MarketingController {
    @Autowired
    private OpenAiChatModel chatModel;

    @GetMapping("/marketing")
    public String getMarketing() {
        return "marketing-request";
    }

    private static final Message systemMessage = new SystemMessage("""
                너는 전문 마케팅 카피라이터야.
                입력된 제품 정보를 기반으로 온라인 쇼핑몰/블로그/홍보 페이지에 사용할 매력적인 마케팅 문구를 작성해 줘.
                작성 조건
                1. 소비자의 관심을 끌 수 있도록 첫 문장은 강렬하거나 공감 가는 표현을 사용해.
                2. 제품 특징을 자연스럽게 녹여서 장점이 잘 드러나게 작성해.
                3. 가격과 구매 링크는 구매를 자극하는 문구와 함께 포함시켜.
                   - 예: "지금 2,400원에 만나보세요 👉 http://example.com/p123"
                4. 글자 수는 약 300~500자로 하고, 캐주얼하지만 설득력 있는 톤으로 작성해.
                5. 필요하면 감각적인 이모지도 활용해.
                
                6. Markdown 형식으로 작성해 주세요.
                - 제목(H2) 사용
                - 문단은 빈 줄로 구분
                - 핵심 특징은 Bullet List 사용
                - 적절한 강조(**텍스트**) 사용
            """);

    private static final PromptTemplate promptTemplate = new PromptTemplate("""
                ### 입력 정보
                - 제품명: {name}
                - 가격: {price}
                - 구매 링크: {link}
                - 제품 특징: {features}
            """);

    @PostMapping("/marketing")
    public String postMarketing(String name, String price, String link, String features, Model model) {
        var userMessage = promptTemplate.createMessage(Map.of(
                "name", name,
                "price", price,
                "link", link,
                "features", features));

        String result = chatModel.call(systemMessage, userMessage);
        model.addAttribute("result", result);

        // AI가 생성한 Markdown을 HTML로 변환하여
        // 제목, 목록, 줄바꿈 등이 브라우저에서 정상적으로 렌더링되도록 처리
        Parser parser = Parser.builder().build();
        Node document = parser.parse(result);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);
        model.addAttribute("html", html);

        return "marketing-response";
    }
}






