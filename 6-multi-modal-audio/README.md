
## Audio models
- gpt-audio-1.5
- gpt-audio, gpt-audio-mini (deprecated)
- how to configure
```java
var chatOptions = OpenAiChatOptions.builder()
        .model("gpt-audio-mini")
        .build();
```
```properties
spring.ai.openai.chat.options.model=gpt-audio-mini
```
## Price
- Audio tokens in user messages are 1 token per 100 ms of audio, while audio tokens in assistant messages are 1 token per 50ms of audio.
- Input $32 per 1M tokens, Output $64 per 1M tokens
- 1 hour audio input = ~$3.6, 1 hour audio output = ~$14.4
