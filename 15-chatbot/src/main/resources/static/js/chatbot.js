(() => {
  const STORAGE_KEY = 'campus-chat-auth';
  const loginScreen = document.getElementById('login-screen');
  const chatScreen = document.getElementById('chat-screen');
  const loginForm = document.getElementById('login-form');
  const loginUsername = document.getElementById('login-username');
  const loginPassword = document.getElementById('login-password');
  const loginMessage = document.getElementById('login-message');
  const loginButton = document.getElementById('login-button');
  const chatBanner = document.getElementById('chat-banner');
  const currentUserLabel = document.getElementById('current-user-label');
  const logoutButton = document.getElementById('logout-button');
  const chatScroll = document.getElementById('chat-scroll');
  const emptyState = document.getElementById('empty-state');
  const messageList = document.getElementById('message-list');
  const composerForm = document.getElementById('composer-form');
  const composerInput = document.getElementById('chat-input');
  const composerButton = document.getElementById('composer-button');
  const quickActions = document.querySelectorAll('[data-prompt]');

  const state = {
    auth: null,
    currentUser: '',
    isStreaming: false,
    abortController: null,
    activeAssistantMessage: null,
  };

  init();

  function init() {
    loginForm.addEventListener('submit', handleLoginSubmit);
    logoutButton.addEventListener('click', handleLogout);
    composerForm.addEventListener('submit', handleComposerSubmit);
    composerInput.addEventListener('keydown', handleComposerKeydown);
    composerInput.addEventListener('input', autoResizeComposer);
    quickActions.forEach((button) => {
      button.addEventListener('click', () => {
        composerInput.value = button.dataset.prompt || '';
        autoResizeComposer();
        composerInput.focus();
      });
    });

    restoreSession().finally(() => {
      autoResizeComposer();
      composerInput.value = '';
    });
  }

  async function restoreSession() {
    const storedAuth = readAuth();
    if (!storedAuth) {
      showLogin();
      return;
    }

    showLogin('로그인 정보를 확인하는 중입니다...');

    try {
      const response = await fetch('/chats', {
        method: 'HEAD',
        headers: {
          Authorization: storedAuth.token,
          Accept: 'text/event-stream',
          'Content-Type': 'text/plain',
        },
      });

      if (response.status === 401 || response.status === 403) {
        clearAuth();
        showLogin('세션이 만료되었습니다. 다시 로그인해 주세요.');
        return;
      }

      setAuthenticated(storedAuth);
    } catch (error) {
      showLogin('서버 연결에 실패했습니다. 잠시 후 다시 시도해 주세요.');
    }
  }

  async function handleLoginSubmit(event) {
    event.preventDefault();
    clearLoginMessage();

    const username = loginUsername.value.trim();
    const password = loginPassword.value;

    if (!username || !password) {
      showLoginMessage('사용자 이름과 패스워드를 모두 입력해 주세요.');
      return;
    }

    setLoginPending(true);

    const auth = {
      username,
      token: buildBasicAuth(username, password),
    };

    try {
      const response = await fetch('/chats', {
        method: 'HEAD',
        headers: {
          Authorization: auth.token,
          Accept: 'text/event-stream',
          'Content-Type': 'text/plain',
        },
      });

      if (response.status === 401 || response.status === 403) {
        showLoginMessage('로그인 정보가 올바르지 않습니다.');
        return;
      }

      saveAuth(auth);
      setAuthenticated(auth);
      loginForm.reset();
    } catch (error) {
      showLoginMessage('로그인 요청 중 오류가 발생했습니다.');
    } finally {
      setLoginPending(false);
    }
  }

  function setAuthenticated(auth) {
    state.auth = auth;
    state.currentUser = auth.username;
    currentUserLabel.textContent = auth.username;
    clearLoginMessage();
    showChat();
    clearBanner();
    resetChatView();
    composerInput.focus();
  }

  function showLogin(message = '') {
    stopStreaming();
    chatScreen.classList.add('hidden');
    loginScreen.classList.remove('hidden');
    clearBanner();
    if (message) {
      showLoginMessage(message);
    } else {
      clearLoginMessage();
    }
    loginUsername.focus();
  }

  function showChat() {
    loginScreen.classList.add('hidden');
    chatScreen.classList.remove('hidden');
  }

  function setLoginPending(isPending) {
    loginButton.disabled = isPending;
    loginUsername.disabled = isPending;
    loginPassword.disabled = isPending;
    loginButton.querySelector('.button-label').textContent = isPending ? '확인 중...' : '로그인';
  }

  function showLoginMessage(message) {
    loginMessage.textContent = message;
  }

  function clearLoginMessage() {
    loginMessage.textContent = '';
  }

  function showBanner(message, type = 'info') {
    chatBanner.hidden = false;
    chatBanner.classList.toggle('is-error', type === 'error');
    chatBanner.innerHTML = `<div class="chat-banner__content">${escapeHtml(message)}</div>`;
  }

  function clearBanner() {
    chatBanner.hidden = true;
    chatBanner.classList.remove('is-error');
    chatBanner.textContent = '';
  }

  function resetChatView() {
    messageList.replaceChildren();
    emptyState.classList.remove('hidden');
    composerInput.value = '';
    autoResizeComposer();
    updateComposerMode();
    scrollChatToBottom();
  }

  function handleComposerKeydown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      composerForm.requestSubmit();
    }
  }

  async function handleComposerSubmit(event) {
    event.preventDefault();

    if (state.isStreaming) {
      stopStreaming();
      return;
    }

    const message = composerInput.value.trim();
    if (!message || !state.auth) {
      return;
    }

    clearBanner();
    appendUserMessage(message);
    composerInput.value = '';
    autoResizeComposer();
    setEmptyStateVisible(false);

    const assistantMessage = createAssistantMessage();
    state.activeAssistantMessage = assistantMessage;
    updateComposerMode(true);
    showBanner('응답을 생성하는 중입니다. 중지 버튼으로 스트리밍을 끊을 수 있습니다.');

    try {
      state.abortController = new AbortController();
      state.isStreaming = true;
      updateComposerMode(true);

      const response = await fetch('/chats', {
        method: 'POST',
        headers: {
          Authorization: state.auth.token,
          'Content-Type': 'text/plain',
          Accept: 'text/event-stream',
        },
        body: message,
        signal: state.abortController.signal,
      });

      if (response.status === 401 || response.status === 403) {
        handleAuthExpired();
        return;
      }

      if (!response.ok) {
        finalizeAssistantMessage(assistantMessage, '응답을 받아오지 못했습니다. 다시 시도해 주세요.');
        showBanner('서버와 통신하는 중 오류가 발생했습니다.', 'error');
        return;
      }

      await streamSseResponse(response, (chunk) => {
        appendAssistantChunk(assistantMessage, chunk);
      });

      finalizeAssistantMessage(assistantMessage);
      clearBanner();
    } catch (error) {
      if (error.name === 'AbortError') {
        finalizeAssistantMessage(assistantMessage, '응답이 중단되었습니다.');
        showBanner('응답 생성을 중단했습니다.');
      } else {
        finalizeAssistantMessage(assistantMessage, '응답을 받아오지 못했습니다. 다시 시도해 주세요.');
        showBanner('서버와 통신하는 중 오류가 발생했습니다.', 'error');
      }
    } finally {
      state.isStreaming = false;
      state.abortController = null;
      state.activeAssistantMessage = null;
      updateComposerMode(false);
      composerInput.focus();
    }
  }

  function appendUserMessage(text) {
    const message = buildMessageRow('user', text, '나');
    messageList.appendChild(message.element);
    scrollChatToBottom();
  }

  function createAssistantMessage() {
    const now = formatTime(new Date());
    const row = document.createElement('article');
    row.className = 'message-row ai';

    const avatar = document.createElement('div');
    avatar.className = 'message-avatar';
    avatar.setAttribute('aria-hidden', 'true');
    avatar.innerHTML = `
      <svg viewBox="0 0 24 24" class="message-avatar__svg" focusable="false">
        <path d="M20 11.5c0 4.14-3.58 7.5-8 7.5-1.07 0-2.1-.19-3.03-.53L5 20l1.24-3.32A7.1 7.1 0 0 1 4 11.5C4 7.36 7.58 4 12 4s8 3.36 8 7.5Zm-11-1.25a1.25 1.25 0 1 0 0 2.5 1.25 1.25 0 0 0 0-2.5Zm3 0a1.25 1.25 0 1 0 0 2.5 1.25 1.25 0 0 0 0-2.5Zm3 0a1.25 1.25 0 1 0 0 2.5 1.25 1.25 0 0 0 0-2.5Z"></path>
      </svg>
    `;

    const card = document.createElement('div');
    card.className = 'message-card';

    const bubble = document.createElement('div');
    bubble.className = 'message-bubble';
    bubble.textContent = '...';
    bubble.dataset.placeholder = 'true';

    const meta = document.createElement('div');
    meta.className = 'message-meta';
    meta.textContent = `챗봇 · ${now}`;

    card.append(bubble, meta);
    row.append(avatar, card);
    messageList.appendChild(row);
    scrollChatToBottom();

    return { row, bubble, meta, placeholder: true };
  }

  function buildMessageRow(role, text, label) {
    const now = formatTime(new Date());
    const row = document.createElement('article');
    row.className = `message-row ${role}`;

    const card = document.createElement('div');
    card.className = 'message-card';

    const bubble = document.createElement('div');
    bubble.className = 'message-bubble';
    bubble.textContent = text;

    const meta = document.createElement('div');
    meta.className = 'message-meta';
    meta.textContent = `${label} · ${now}`;

    card.append(bubble, meta);

    if (role === 'ai') {
      const avatar = document.createElement('div');
      avatar.className = 'message-avatar';
      avatar.setAttribute('aria-hidden', 'true');
      avatar.innerHTML = `
        <svg viewBox="0 0 24 24" class="message-avatar__svg" focusable="false">
          <path d="M20 11.5c0 4.14-3.58 7.5-8 7.5-1.07 0-2.1-.19-3.03-.53L5 20l1.24-3.32A7.1 7.1 0 0 1 4 11.5C4 7.36 7.58 4 12 4s8 3.36 8 7.5Zm-11-1.25a1.25 1.25 0 1 0 0 2.5 1.25 1.25 0 0 0 0-2.5Zm3 0a1.25 1.25 0 1 0 0 2.5 1.25 1.25 0 0 0 0-2.5Zm3 0a1.25 1.25 0 1 0 0 2.5 1.25 1.25 0 0 0 0-2.5Z"></path>
        </svg>
      `;
      row.append(avatar, card);
    } else {
      row.append(card);
    }

    return { element: row, bubble, meta };
  }

  function appendAssistantChunk(message, chunk) {
    if (message.bubble.dataset.placeholder === 'true') {
      message.bubble.textContent = chunk;
      message.bubble.dataset.placeholder = 'false';
      scrollChatToBottom();
      return;
    }

    message.bubble.textContent += chunk;
    scrollChatToBottom();
  }

  function finalizeAssistantMessage(message, fallbackText) {
    if (message.bubble.dataset.placeholder === 'true') {
      message.bubble.dataset.placeholder = 'false';
      message.bubble.textContent = fallbackText || '응답을 생성하지 못했습니다.';
    }

    if (!message.bubble.textContent.trim()) {
      message.bubble.textContent = fallbackText || '응답을 생성하지 못했습니다.';
    }

    scrollChatToBottom();
  }

  async function streamSseResponse(response, onChunk) {
    if (!response.body) {
      throw new Error('ReadableStream is not available.');
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';
    let eventData = [];

    const flushLine = (line) => {
      if (line.startsWith('data:')) {
        eventData.push(line.slice(5).replace(/^ /, ''));
        return;
      }

      if (line.startsWith(':')) {
        return;
      }

      if (line === '') {
        emitEvent();
      }
    };

    const emitEvent = () => {
      if (!eventData.length) {
        return;
      }

      const raw = eventData.join('\n');
      eventData = [];
      onChunk(parseStreamData(raw));
    };

    try {
      while (true) {
        const { value, done } = await reader.read();
        if (done) {
          break;
        }

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split(/\r?\n/);
        buffer = lines.pop() || '';

        for (const line of lines) {
          flushLine(line);
        }
      }

      buffer += decoder.decode();
      if (buffer.length > 0) {
        const lines = buffer.split(/\r?\n/);
        for (const line of lines) {
          flushLine(line);
        }
      }

      emitEvent();
    } finally {
      reader.releaseLock();
    }
  }

  function parseStreamData(raw) {
    try {
      const parsed = JSON.parse(raw);
      return typeof parsed === 'string' ? parsed : String(parsed);
    } catch (error) {
      return raw;
    }
  }

  function handleAuthExpired() {
    clearAuth();
    stopStreaming();
    showLogin('인증이 만료되었습니다. 다시 로그인해 주세요.');
  }

  function handleLogout() {
    stopStreaming();
    clearAuth();
    clearBanner();
    loginForm.reset();
    clearLoginMessage();
    resetChatView();
    showLogin();
  }

  function stopStreaming() {
    if (state.abortController) {
      state.abortController.abort();
    }
  }

  function updateComposerMode(isStreaming) {
    composerButton.classList.toggle('is-streaming', isStreaming);
    composerButton.setAttribute('aria-label', isStreaming ? '중지' : '전송');
    composerButton.querySelector('.composer-button__text').textContent = isStreaming ? '중지' : '전송';
    composerInput.disabled = false;
  }

  function setEmptyStateVisible(visible) {
    emptyState.classList.toggle('hidden', !visible);
  }

  function scrollChatToBottom() {
    requestAnimationFrame(() => {
      chatScroll.scrollTop = chatScroll.scrollHeight;
    });
  }

  function autoResizeComposer() {
    composerInput.style.height = 'auto';
    const nextHeight = Math.min(composerInput.scrollHeight, 152);
    composerInput.style.height = `${Math.max(nextHeight, 48)}px`;
  }

  function buildBasicAuth(username, password) {
    const bytes = new TextEncoder().encode(`${username}:${password}`);
    let binary = '';
    bytes.forEach((byte) => {
      binary += String.fromCharCode(byte);
    });
    return `Basic ${btoa(binary)}`;
  }

  function formatTime(date) {
    return new Intl.DateTimeFormat('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    }).format(date);
  }

  function saveAuth(auth) {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
  }

  function readAuth() {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw);
    } catch (error) {
      sessionStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }

  function clearAuth() {
    sessionStorage.removeItem(STORAGE_KEY);
    state.auth = null;
    state.currentUser = '';
    currentUserLabel.textContent = '';
  }

  function escapeHtml(text) {
    return text
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }
})();



