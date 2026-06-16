# Spring AI Chat Memory 예제

Spring AI의 `ChatMemory`를 MariaDB에 저장하면서 `ChatModel`과 `ChatClient` 방식으로 대화 이력을 처리하는 예제입니다.

## 1. 프로젝트 생성

이 프로젝트는 Spring Boot, Spring AI OpenAI, JDBC 기반 Chat Memory 저장소, MariaDB 드라이버를 사용합니다.

주요 의존성은 [build.gradle](build.gradle)에 설정되어 있습니다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-webmvc'
implementation 'org.springframework.ai:spring-ai-starter-model-openai'
implementation 'org.springframework.ai:spring-ai-starter-model-chat-memory-repository-jdbc'
runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
```

### OPENAI_API_KEY 환경변수 설정

OpenAI API를 호출하려면 실행 환경에 `OPENAI_API_KEY`를 설정해야 합니다.

PowerShell:

```powershell
$env:OPENAI_API_KEY="your-openai-api-key"
```

Windows 영구 설정:

```powershell
setx OPENAI_API_KEY "your-openai-api-key"
```

### MariaDB 준비

Docker로 MariaDB를 실행할 수 있습니다.

```bash
docker run -d \
  --name mariadb \
  -p 3306:3306 \
  -e MARIADB_ROOT_PASSWORD=rootpass \
  -e MARIADB_DATABASE=mydb \
  -e MARIADB_USER=myuser \
  -e MARIADB_PASSWORD=mypass \
  mariadb
```

### datasource 설정

[src/main/resources/application.properties](src/main/resources/application.properties)에 MariaDB 연결 정보를 설정합니다.

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/mydb
spring.datasource.username=myuser
spring.datasource.password=mypass
```

### Chat Memory 스키마 생성 설정

Spring AI JDBC Chat Memory 저장소가 사용할 테이블을 자동 생성하도록 다음 설정을 추가합니다.

```properties
spring.ai.chat.memory.repository.jdbc.initialize-schema=always
```

개발 환경에서는 `always`로 두면 편리하지만, 운영 환경에서는 스키마 관리 방식을 별도로 정하는 것이 좋습니다.

## 2. ChatModel 방식

[ChatBotController](src/main/java/com/example/demo/controller/ChatBotController.java)의 `postChatsWithModel` 메서드는 `OpenAiChatModel`과 `ChatMemory`를 직접 사용합니다.

처리 흐름은 다음과 같습니다.

1. Conversation Id에 해당하는 ChatMemory가 처음 생성되는 경우 시스템 메시지를 추가합니다.
2. 사용자가 보낸 메시지를 `UserMessage`로 만들어 ChatMemory에 추가합니다.
3. ChatMemory에 저장된 전체 메시지 목록으로 `Prompt`를 생성합니다.
4. `OpenAiChatModel`이 답변을 생성합니다.
5. GPT가 생성한 답변을 어시스턴트 메시지로 ChatMemory에 다시 추가합니다.

핵심 코드는 다음 구조입니다.

```java
if (chatMemory.get(id).isEmpty()) {
    chatMemory.add(id, new SystemMessage("정확하고 명료하게 답변해 주세요."));
}

var userMessage = new UserMessage(message);
chatMemory.add(id, userMessage);

var prompt = new Prompt(chatMemory.get(id));
var chatResponse = chatModel.call(prompt);

var assistantMessage = Objects.requireNonNull(chatResponse.getResult()).getOutput();
chatMemory.add(id, assistantMessage);
```

현재 이 방식의 엔드포인트 매핑은 주석 처리되어 있습니다.

```java
//@PostMapping("/chats")
```

## 3. ChatClient 방식

[AiConfig](src/main/java/com/example/demo/config/AiConfig.java)에서는 `ChatClient`를 Bean으로 등록하면서 기본 시스템 메시지와 기본 Advisor를 설정합니다.

```java
return builder
        .defaultSystem("정확하고 명료하게 답변해 주세요.")
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();
```

여기서 `MessageChatMemoryAdvisor`가 `ChatMemory` 저장과 조회를 담당하므로, 컨트롤러에서 사용자 메시지와 어시스턴트 메시지를 직접 저장하지 않아도 됩니다.

[ChatBotController](src/main/java/com/example/demo/controller/ChatBotController.java)의 `/chats` 엔드포인트에서는 사용자 메시지를 처리할 때 Conversation Id만 Advisor 파라미터로 전달합니다.

```java
@PostMapping("/chats")
public String postChats(@RequestParam("id") String id, @RequestBody String message) {
    return chatClient.prompt()
            .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, id))
            .user(message)
            .call().content();
}
```

요청 예시는 다음과 같습니다.

```bash
curl -X POST "http://localhost:8080/chats?id=user-1" \
  -H "Content-Type: text/plain" \
  -d "Spring AI ChatMemory가 뭐야?"
```

같은 `id`로 계속 요청하면 이전 대화 이력을 기반으로 답변합니다.

## 실행

MariaDB와 `OPENAI_API_KEY` 설정을 완료한 뒤 애플리케이션을 실행합니다.

```bash
./gradlew bootRun
```

Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```
