package com.example.demo;

import com.openai.models.audio.AudioResponseFormat;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
public class TranscriptionModelTests {
    private static final Logger log = LoggerFactory.getLogger(TranscriptionModelTests.class);

    @Autowired
    private OpenAiAudioTranscriptionModel transcriptionModel;

    @Test
    public void testTranscriptModelSimple() throws IOException {
        //Resource resource = new ClassPathResource("/audio/voc_kart_rider.mp3");
        //Resource resource = new UrlResource("https://xxx/sample_audio.mp3");
        Resource resource = new FileSystemResource("D:/hackers/lecture/sample/crew-interview-1.mp3");
        String script = transcriptionModel.transcribe(resource);
        Files.writeString(Paths.get("D:/hackers/lecture/output/interview.txt"), script);
    }

    /**
     * OpenAI Audio Transcription 옵션 설정 예제
     *
     * - model
     *   whisper-1(default), gpt-4o-transcribe, gpt-4o-mini-transcribe 지원
     *
     * - language
     *   번역할 언어 ko, en, ja 등 90개 이상의 언어 지원
     *   설정하지 않으면 자동 감지(auto detect)
     *
     * - responseFormat
     *   VTT(WebVTT: Web Video Text Track) -> HTML5 video 태그 기반 웹 자막 포맷
     *   SRT(SubRip Subtitle) -> 현재 가장 많이 사용하는 범용 자막 포맷
     */
    @Test
    public void testTranscriptModelOptions() throws IOException {
        OpenAiAudioTranscriptionOptions openAiAudioTranscriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .model("whisper-1")
                .language("ko")
                .responseFormat(AudioResponseFormat.SRT)
                .build();

        Resource resource = new FileSystemResource("D:/hackers/lecture/sample/crew-interview-2.mp3");
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource, openAiAudioTranscriptionOptions);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);

        Files.writeString(Paths.get("D:/hackers/lecture/output/interview.srt"), response.getResult().getOutput());
    }
}
