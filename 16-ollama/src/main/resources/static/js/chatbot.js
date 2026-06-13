(() => {
    const loginScreen = document.getElementById("login-screen");
    const chatScreen = document.getElementById("chat-screen");
    const loginForm = document.getElementById("login-form");
    const messageForm = document.getElementById("message-form");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const loginError = document.getElementById("login-error");
    const chatError = document.getElementById("chat-error");
    const messagesContainer = document.getElementById("chat-messages");
    const messageInput = document.getElementById("message-input");
    const sendButton = document.getElementById("send-button");
    const logoutButton = document.getElementById("logout-button");

    const state = {
        credentials: null,
        streamController: null,
        isStreaming: false
    };

    addBotMessage("안녕하세요. 무엇을 도와드릴까요?");

    loginForm.addEventListener("submit", (event) => {
        event.preventDefault();
        clearError(loginError);

        const username = usernameInput.value.trim();
        const password = passwordInput.value;

        if (!username || !password) {
            showError(loginError, "사용자 이름과 패스워드를 모두 입력해 주세요.");
            return;
        }

        state.credentials = {username, password};
        switchToChatScreen();
    });

    logoutButton.addEventListener("click", () => {
        stopStreaming();
        state.credentials = null;
        resetChat();
        switchToLoginScreen();
    });

    messageForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (state.isStreaming) {
            stopStreaming();
            return;
        }

        clearError(chatError);

        const message = messageInput.value.trim();
        if (!message) {
            return;
        }

        if (!state.credentials) {
            showError(chatError, "인증 정보가 없습니다. 다시 로그인해 주세요.");
            switchToLoginScreen();
            return;
        }

        messageInput.value = "";
        addUserMessage(message);

        const botBubble = addBotMessage("");

        try {
            await streamChatResponse(message, botBubble);
        } catch (error) {
            if (error.name === "AbortError") {
                return;
            }

            if (error.isAuthError) {
                showError(loginError, error.message);
                return;
            }

            if (error.name !== "AbortError") {
                showError(chatError, error.message || "요청 처리 중 오류가 발생했습니다.");
            }
        } finally {
            setStreamingState(false);
            state.streamController = null;
            focusMessageInput();
        }
    });

    async function streamChatResponse(message, botBubble) {
        setStreamingState(true);
        const controller = new AbortController();
        state.streamController = controller;

        const response = await fetch("/chats", {
            method: "POST",
            headers: {
                "Authorization": buildBasicAuthHeader(state.credentials),
                "Content-Type": "text/plain",
                "Accept": "text/event-stream"
            },
            body: message,
            signal: controller.signal
        });

        if (response.status === 401) {
            state.credentials = null;
            switchToLoginScreen();
            const error = new Error("인증에 실패했습니다. 사용자 이름과 패스워드를 확인해 주세요.");
            error.isAuthError = true;
            throw error;
        }

        if (response.status === 403) {
            throw new Error("접근 권한이 없습니다.");
        }

        if (!response.ok || !response.body) {
            throw new Error("서버 응답 처리에 실패했습니다.");
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = "";

        while (true) {
            const {done, value} = await reader.read();
            if (done) {
                processSseChunk(buffer, botBubble, true);
                break;
            }

            buffer += decoder.decode(value, {stream: true});
            buffer = processSseChunk(buffer, botBubble, false);
        }
    }

    // SSE 프레임 경계(빈 줄) 기준으로 이벤트를 분리해 안전하게 누적 렌더링한다.
    function processSseChunk(buffer, botBubble, flushRemainder) {
        const chunks = buffer.split("\n\n");
        const eventChunks = flushRemainder ? chunks : chunks.slice(0, -1);

        for (const chunk of eventChunks) {
            const lines = chunk.split("\n");
            for (const rawLine of lines) {
                const line = rawLine.trim();
                if (!line || !line.startsWith("data:")) {
                    continue;
                }

                const payload = line.slice(5).trim();
                if (!payload) {
                    continue;
                }

                try {
                    const token = JSON.parse(payload);
                    if (typeof token === "string") {
                        botBubble.textContent += token;
                        scrollMessagesToBottom();
                    }
                } catch {
                    // JSON 파싱에 실패한 토큰은 무시하고 다음 토큰을 계속 처리한다.
                }
            }
        }

        return flushRemainder ? "" : chunks[chunks.length - 1];
    }

    function stopStreaming() {
        if (!state.streamController) {
            return;
        }

        state.streamController.abort();
    }

    function setStreamingState(isStreaming) {
        state.isStreaming = isStreaming;
        sendButton.textContent = isStreaming ? "중단" : "전송";
        sendButton.classList.toggle("is-streaming", isStreaming);
        messageInput.disabled = isStreaming;
        messageInput.placeholder = isStreaming ? "응답을 수신 중입니다..." : "메시지를 입력하세요";
    }

    function addUserMessage(text) {
        addMessage("user", text);
    }

    function addBotMessage(text) {
        return addMessage("bot", text);
    }

    function addMessage(role, text) {
        const row = document.createElement("div");
        row.className = `message-row ${role}`;

        const bubble = document.createElement("div");
        bubble.className = "message-bubble";
        bubble.textContent = text;

        row.appendChild(bubble);
        messagesContainer.appendChild(row);
        scrollMessagesToBottom();

        return bubble;
    }

    function switchToChatScreen() {
        loginScreen.classList.add("hidden");
        chatScreen.classList.remove("hidden");
        focusMessageInput();
        clearError(chatError);
    }

    function switchToLoginScreen() {
        chatScreen.classList.add("hidden");
        loginScreen.classList.remove("hidden");
        passwordInput.value = "";
        usernameInput.focus();
    }

    function resetChat() {
        messagesContainer.innerHTML = "";
        addBotMessage("안녕하세요. 무엇을 도와드릴까요?");
        clearError(chatError);
        setStreamingState(false);
    }

    function focusMessageInput() {
        if (chatScreen.classList.contains("hidden") || messageInput.disabled) {
            return;
        }

        requestAnimationFrame(() => {
            messageInput.focus();
        });
    }

    function showError(errorElement, message) {
        errorElement.textContent = message;
    }

    function clearError(errorElement) {
        errorElement.textContent = "";
    }

    function scrollMessagesToBottom() {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    function buildBasicAuthHeader(credentials) {
        const source = `${credentials.username}:${credentials.password}`;
        return `Basic ${btoa(unescape(encodeURIComponent(source)))}`;
    }
})();


