package com.example.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.moderation.ModerationOptions;
import org.springframework.ai.moderation.ModerationOptionsBuilder;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.ai.openai.OpenAiModerationOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SpringBootTest
public class ModerationTests {
    private static final Logger log = LoggerFactory.getLogger(ModerationTests.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private OpenAiModerationModel moderationModel;

    @Test
    public void testModeration() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:serial-killer-en.txt");
        String text = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        var prompt = new ModerationPrompt(text);
        var response = moderationModel.call(prompt);

        var moderation = response.getResult().getOutput();

        log.info("Model used: {}", moderation.getModel());
        for (var result : moderation.getResults()) {
            log.info("Moderation Result:");
            log.info("Flagged: {}", result.isFlagged());

            // 각 카테고리별 위험 여부
            var categories = result.getCategories();
            log.info("Categories:");
            log.info("Financial(금융 관련 정보): {}", categories.isFinancial());
            log.info("Legal(법률 관련 정보): {}", categories.isLaw());
            log.info("PII(개인 식별 정보): {}", categories.isPii());
            log.info("Dangerous and Criminal Content(위험하거나 불법적인 행위): {}", categories.isDangerousAndCriminalContent());
            log.info("Sexual(성적인 컨텐츠): {}", categories.isSexual());
            log.info("Hate(증오 또는 혐오): {}", categories.isHate());
            log.info("Harassment(괴롭힘 또는 희롱): {}", categories.isHarassment());
            log.info("Self-Harm(자해): {}", categories.isSelfHarm());
            log.info("Sexual/Minors(미성년자와 관련된 성적인 컨텐츠): {}", categories.isSexualMinors());
            log.info("Hate/Threatening(위협적인 증오 발언): {}", categories.isHateThreatening());
            log.info("Violence/Graphic(폭력적이고 잔인한 묘사): {}", categories.isViolenceGraphic());
            log.info("Self-Harm/Intent(자해 의도): {}", categories.isSelfHarmIntent());
            log.info("Self-Harm/Instructions(자해 방법에 대한 지침): {}", categories.isSelfHarmInstructions());
            log.info("Harassment/Threatening(협박성 괴롭힘): {}", categories.isHarassmentThreatening());
            log.info("Violence(폭력): {}", categories.isViolence());

            // 각 카테고리별 위험 확률
            // OpenAI는 정확한 임계값(threshold)를 공개하지 않았으나 0.0 ~ 1.1 사이 값중 대략 0.8 이상은 경우 위험으로 판단
            var scores = result.getCategoryScores();
            log.info("Category Scores:");
            log.info("Financial(금융 관련 정보): {}", scores.getFinancial());
            log.info("Legal(법률 관련 정보): {}", scores.getLaw());
            log.info("PII(개인 식별 정보): {}", scores.getPii());
            log.info("Dangerous and Criminal Content(위험하거나 불법적인 행위): {}", scores.getDangerousAndCriminalContent());
            log.info("Sexual(괴롭힘 또는 희롱): {}", scores.getSexual());
            log.info("Hate(증오 또는 혐오): {}", scores.getHate());
            log.info("Harassment(괴롭힘 또는 희롱): {}", scores.getHarassment());
            log.info("Self-Harm(자해): {}", scores.getSelfHarm());
            log.info("Sexual/Minors(미성년자와 관련된 성적인 컨텐츠): {}", scores.getSexualMinors());
            log.info("Hate/Threatening(위협적인 증오 발언): {}", scores.getHateThreatening());
            log.info("Violence/Graphic(폭력적이고 잔인한 묘사): {}", scores.getViolenceGraphic());
            log.info("Self-Harm/Intent(자해 의도): {}", scores.getSelfHarmIntent());
            log.info("Self-Harm/Instructions(자해 방법에 대한 지침): {}", scores.getSelfHarmInstructions());
            log.info("Harassment/Threatening(협박성 괴롭힘): {}", scores.getHarassmentThreatening());
            log.info("Violence(폭력): " + scores.getViolence());
        }
    }
}
