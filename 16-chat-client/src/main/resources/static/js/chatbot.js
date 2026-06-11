(() => {
    'use strict';

    // 로그인 정보는 localStorage/sessionStorage에 저장하지 않고 메모리에서만 유지한다.
    const state = {
        username: '',
        password: '',
        isSending: false,
        activeAssistantMessageEl: null,
        activeAssistantTextEl: null
    };

    const elements = {
        loginScreen: document.getElementById('login-screen'),
        chatScreen: document.getElementById('chat-screen'),
        loginForm: document.getElementById('login-form'),
        usernameInput: document.getElementById('username'),
        passwordInput: document.getElementById('password'),
        loginUsername: document.getElementById('login-username'),
        logoutButton: document.getElementById('logout-button'),
        chatForm: document.getElementById('chat-form'),
        chatList: document.getElementById('chat-list'),
        messageInput: document.getElementById('message-input'),
        sendButton: document.getElementById('send-button')
    };

    function setView(isChatView) {
        elements.loginScreen.classList.toggle('hidden', isChatView);
        elements.chatScreen.classList.toggle('hidden', !isChatView);
        if (isChatView) {
            requestAnimationFrame(() => elements.messageInput.focus());
        } else {
            requestAnimationFrame(() => elements.usernameInput.focus());
        }
    }

    function scrollChatToBottom() {
        elements.chatList.scrollTop = elements.chatList.scrollHeight;
    }

    function createMessageRow(role, text = '') {
        const row = document.createElement('div');
        row.className = `message-row ${role}`;

        if (role === 'ai') {
            const avatar = document.createElement('div');
            avatar.className = 'avatar ai';
            avatar.textContent = 'A';
            row.appendChild(avatar);
        }

        const bubble = document.createElement('div');
        bubble.className = `bubble ${role}`;
        bubble.textContent = text;
        row.appendChild(bubble);

        if (role === 'user') {
            const avatar = document.createElement('div');
            avatar.className = 'avatar user';
            avatar.textContent = 'U';
            row.appendChild(avatar);
        }

        elements.chatList.appendChild(row);
        scrollChatToBottom();
        return { row, bubble };
    }

    function appendSystemNote(text, isError = false) {
        const note = document.createElement('div');
        note.className = `system-note ${isError ? 'error-note' : ''}`.trim();
        note.textContent = text;
        elements.chatList.appendChild(note);
        scrollChatToBottom();
        return note;
    }

    function appendUserMessage(text) {
        return createMessageRow('user', text);
    }

    function appendAssistantMessage(initialText = '') {
        const { row, bubble } = createMessageRow('ai', initialText);
        state.activeAssistantMessageEl = row;
        state.activeAssistantTextEl = bubble;
        return bubble;
    }

    function updateSendState(isSending) {
        state.isSending = isSending;
        elements.sendButton.disabled = isSending;
        elements.messageInput.disabled = isSending;
        elements.logoutButton.disabled = isSending;
        elements.loginForm.querySelectorAll('input').forEach((input) => {
            input.disabled = isSending;
        });
    }

    function buildBasicAuthHeader(username, password) {
        return 'Basic ' + btoa(`${username}:${password}`);
    }

    function resetChatArea() {
        elements.chatList.innerHTML = '';
    }

    function showWelcomeMessages() {
        appendAssistantMessage('안녕하세요.\n해커스캠퍼스 고객센터입니다.\n무엇을 도와드릴까요?');
    }

    function resetSession() {
        state.username = '';
        state.password = '';
        state.activeAssistantMessageEl = null;
        state.activeAssistantTextEl = null;
        updateSendState(false);
        resetChatArea();
        elements.loginForm.reset();
        setView(false);
    }

    function initializeChatView(username) {
        state.username = username;
        elements.loginUsername.textContent = username;
        resetChatArea();
        showWelcomeMessages();
        setView(true);
    }

    function resizeTextarea() {
        elements.messageInput.style.height = 'auto';
        elements.messageInput.style.height = `${Math.min(elements.messageInput.scrollHeight, 144)}px`;
    }

    function parseSseBuffer(buffer, onToken, onError) {
        const normalized = buffer.replace(/\r\n/g, '\n');
        const lines = normalized.split('\n');
        const remainder = normalized.endsWith('\n') ? '' : lines.pop() ?? '';

        for (const rawLine of lines) {
            const line = rawLine.trimEnd();
            if (!line) {
                continue;
            }

            if (!line.startsWith('data:')) {
                continue;
            }

            const jsonText = line.substring(5).trimStart();
            if (!jsonText) {
                continue;
            }

            try {
                const token = JSON.parse(jsonText);
                onToken(typeof token === 'string' ? token : String(token));
            } catch (error) {
                onError(error, jsonText);
            }
        }

        return remainder;
    }

    async function sendMessage(message) {
        if (state.isSending) {
            return;
        }

        const trimmedMessage = message.trim();
        if (!trimmedMessage) {
            appendSystemNote('메시지를 입력해 주세요.');
            return;
        }

        appendUserMessage(message);
        const assistantTextEl = appendAssistantMessage('');
        updateSendState(true);
        elements.messageInput.value = '';
        resizeTextarea();

        const authHeader = buildBasicAuthHeader(state.username, state.password);

        try {
            const response = await fetch('/chats', {
                method: 'POST',
                headers: {
                    Authorization: authHeader,
                    'Content-Type': 'text/plain',
                    Accept: 'text/event-stream'
                },
                body: message
            });

            if (!response.ok) {
                const errorText = await response.text().catch(() => '');
                const messageText = `채팅 요청에 실패했습니다. (${response.status}) ${errorText}`.trim();
                assistantTextEl.textContent = messageText;
                appendSystemNote(messageText, true);
                return;
            }

            if (!response.body) {
                const messageText = '스트리밍 응답을 읽을 수 없습니다.';
                assistantTextEl.textContent = messageText;
                appendSystemNote(messageText, true);
                return;
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = '';
            let assistantText = '';

            while (true) {
                const { value, done } = await reader.read();
                if (done) {
                    break;
                }

                buffer += decoder.decode(value, { stream: true });
                buffer = parseSseBuffer(
                    buffer,
                    (token) => {
                        assistantText += token;
                        assistantTextEl.textContent = assistantText;
                        scrollChatToBottom();
                    },
                    (error, jsonText) => {
                        console.warn('SSE token 파싱 실패:', jsonText, error);
                    }
                );
            }

            buffer += decoder.decode();
            parseSseBuffer(
                buffer,
                (token) => {
                    assistantText += token;
                    assistantTextEl.textContent = assistantText;
                    scrollChatToBottom();
                },
                (error, jsonText) => {
                    console.warn('SSE token 파싱 실패:', jsonText, error);
                }
            );

            if (!assistantText.trim()) {
                assistantTextEl.textContent = '응답이 비어 있습니다.';
            }
        } catch (error) {
            console.error(error);
            if (assistantTextEl && assistantTextEl.textContent === '') {
                assistantTextEl.textContent = '응답을 불러오지 못했습니다.';
            }
            appendSystemNote(error instanceof Error ? error.message : '오류가 발생했습니다.', true);
        } finally {
            updateSendState(false);
            state.activeAssistantMessageEl = null;
            state.activeAssistantTextEl = null;
            elements.messageInput.focus();
            scrollChatToBottom();
        }
    }

    elements.loginForm.addEventListener('submit', (event) => {
        event.preventDefault();

        const username = elements.usernameInput.value.trim();
        const password = elements.passwordInput.value;

        if (!username || !password) {
            appendSystemNote('사용자명과 비밀번호를 모두 입력해 주세요.', true);
            return;
        }

        state.username = username;
        state.password = password;
        initializeChatView(username);
    });

    elements.chatForm.addEventListener('submit', (event) => {
        event.preventDefault();
        void sendMessage(elements.messageInput.value);
    });

    elements.messageInput.addEventListener('input', resizeTextarea);
    elements.messageInput.addEventListener('keydown', (event) => {
        if (event.isComposing) {
            return;
        }

        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            void sendMessage(elements.messageInput.value);
        }
    });

    elements.logoutButton.addEventListener('click', () => {
        if (state.isSending) {
            appendSystemNote('응답 수신 중에는 로그아웃할 수 없습니다.', true);
            return;
        }
        resetSession();
    });

    // 초기 상태: 로그인 화면을 보여 주고, 사용자가 바로 입력할 수 있게 포커스를 준다.
    setView(false);
    resizeTextarea();
})();


