package com.example.demo.controller;

import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Objects;

@RestController
public class ApiController {
    @Autowired
    private OpenAiImageModel imageModel;

    @GetMapping(value = "/images", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getImages(@RequestParam("message") String message) {
        var options = OpenAiImageOptions.builder().responseFormat("b64_json").build();
        var response = imageModel.call(new ImagePrompt(message, options));
        var b64Json = Objects.requireNonNull(response.getResult()).getOutput().getB64Json();
        return Base64.getDecoder().decode(b64Json);
    }
}
