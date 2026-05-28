package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechModel;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
public class ElevenLabsSpeechModelTests {
    @Autowired
    private ElevenLabsTextToSpeechModel speechModel;

    private String text = """
            안녕하세요. 해커스 캠퍼스 고객지원센터입니다.
            문의하신 맥북에어는 현재 배송 준비 중입니다.
            예상 도착 시간은 오후 3시이며,
            배송기사 도착전 문자메시지가 발송될 예정입니다.
            추가 문의사항이 있으시면 “상담원 연결”이라고 말씀해 주세요.
            """;

    private String textEng = """
            Hello, this is the Hackers Campus Customer Support Center.
            The MacBook Air you inquired about is currently being prepared for delivery.
            The estimated arrival time is 3:00 PM, and a text message notification will be sent before the delivery driver arrives.
            If you have any additional questions, please say “Connect me to an agent.”
            """;

    @Test
    public void testSpeechModelSimple() throws IOException {
        byte[] bin = speechModel.call(text);
        Files.write(Paths.get("D:/hackers/lecture/output/elevenlabs-simple.mp3"), bin);
    }

    @Test
    public void testSpeechModel() throws IOException {
        // Eunha(cBOtnpVZNlQ5VJygXGB8) - Elegant Korean Female
        // Stella(2vbhUP8zyKg4dEZaTWGn) - Warm and Natural
        ElevenLabsTextToSpeechOptions speechOptions = ElevenLabsTextToSpeechOptions.builder()
                .model("eleven_multilingual_v2") // eleven_v3, eleven_flash_v2_5, eleven_multilingual_v2, etc...
                .voiceId("cBOtnpVZNlQ5VJygXGB8")
                .build();

        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, speechOptions);
        TextToSpeechResponse response = speechModel.call(prompt);

        byte[] bin = response.getResult().getOutput();
        Files.write(Paths.get("D:/hackers/lecture/output/elevenlabs_options.mp3"), bin);
    }
}
