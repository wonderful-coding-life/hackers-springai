package com.example.demo.controller;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
public class ApiController {
    @Autowired
    private OpenAiChatModel chatModel;

    @PostMapping("/meetings/summaries")
    public String postMeetingSummary(@RequestParam("file") MultipartFile file) throws IOException {
        var resource = new InputStreamResource(file.getInputStream());

        // 오디오 파일은 업로드된 Content-Type을 그대로 사용하면 안 될 수 있으므로,
        // 실제 파일 포맷에 맞게 MIME 타입을 명시적으로 지정해야 한다.
        // (예: MP3 → audio/mp3, WAV → audio/wav)
        //
        // 참고:
        // - RFC 표준에서 MP3의 공식 MIME 타입은 audio/mpeg이지만,
        // - 일부 라이브러리/모델에서는 audio/mp3를 요구할 수 있음
        // → 따라서 file.getContentType() 대신 직접 지정하는 것이 안전함
        // var mimeType = MimeTypeUtils.parseMimeType(Objects.requireNonNull(file.getContentType()));
        var mimeType = MimeTypeUtils.parseMimeType("audio/mp3");

        var userMessage = UserMessage.builder()
                .text("회의 내용을 요약해 줘")
                .media(new Media(mimeType, resource))
                .build();

        var chatOptions = OpenAiChatOptions.builder()
                .model("gpt-audio-mini")
                .build();

        var chatResponse = chatModel.call(new Prompt(userMessage, chatOptions));
        return Objects.requireNonNull(chatResponse.getResult()).getOutput().getText();
    }
}
