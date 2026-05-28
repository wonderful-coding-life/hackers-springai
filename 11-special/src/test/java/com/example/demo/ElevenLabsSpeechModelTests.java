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

    @Test
    public void testSpeechModelSimple() throws IOException {
        byte[] bin = speechModel.call("이번역은 해커스, 해커스역입니다. 내리실 문은 오른쪽 입니다. 해커스캠퍼스로 가실 분들은 이번역에서 내리시기 바랍니다.");
        Files.write(Paths.get("D:/hackers/lecture/output/elevenlabs-tts-simple.mp3"), bin);
    }

    @Test
    public void testSpeechModel() throws IOException {
        String text = """
                안녕하세요. 해커스 캠퍼스 고객지원센터입니다.
                문의하신 맥북에어는 현재 배송 준비 중입니다.
                예상 도착 시간은 오후 3시이며,
                배송기사 도착전 문자메시지가 발송될 예정입니다.
                추가 문의사항이 있으시면 “상담원 연결”이라고 말씀해 주세요.
                """;

        String textKor = """
                안녕하세요, 고객님.
                문의 사항이 있으시면 '삐' 소리 후에 음성으로 남겨 주세요.
                확인 후 빠르게 연락드리겠습니다.
                """;

        String textEng = """
                Hello, this is Hackers.
                If you have any questions, please leave a voice message after the beep.
                Thank you!
                """;

        // Eunha(cBOtnpVZNlQ5VJygXGB8) - Elegant Korean Female
        // Jini(0oqpliV6dVSr9XomngOW) - Warm & Intelligent Korean Female
        ElevenLabsTextToSpeechOptions speechOptions = ElevenLabsTextToSpeechOptions.builder()
                .model("eleven_multilingual_v2") // eleven_v3, eleven_flash_v2_5, eleven_multilingual_v2, etc...
                .voiceId("cBOtnpVZNlQ5VJygXGB8")
                .build();

        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, speechOptions);
        TextToSpeechResponse response = speechModel.call(prompt);

        byte[] bin = response.getResult().getOutput();
        Files.write(Paths.get("D:/hackers/lecture/output/elevenlabs_tts_options_eunha.mp3"), bin);
    }
}
