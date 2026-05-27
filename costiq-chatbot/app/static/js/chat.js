/* ── CostIQ ChatBot — Frontend Logic ─────────────────────────────────── */

// Conversation history sent to backend on every turn
const conversationHistory = [];
let   isStreaming         = false;
let   excelReady          = false;

// ── Marked.js config (markdown renderer) ─────────────────────────────────
marked.setOptions({
  breaks:   true,
  gfm:      true,
  sanitize: false,
});


// ── Message rendering ─────────────────────────────────────────────────────

function appendMessage(role, content, isStreaming = false) {
  const container  = document.getElementById("chatMessages");
  const wrapper    = document.createElement("div");
  const isUser     = role === "user";

  wrapper.className = `message ${isUser ? "user-message" : "assistant-message"}`;
  wrapper.id        = isStreaming ? "streaming-msg" : "";

  const avatar = document.createElement("div");
  avatar.className = "message-avatar";
  avatar.innerHTML = isUser
    ? '<i class="bi bi-person-fill"></i>'
    : '<i class="bi bi-robot"></i>';

  const bubble = document.createElement("div");
  bubble.className = "message-bubble";
  bubble.id        = isStreaming ? "streaming-bubble" : "";

  if (isStreaming) {
    // Start with typing indicator
    bubble.innerHTML = `<div class="typing-dots">
      <span></span><span></span><span></span>
    </div>`;
  } else {
    bubble.innerHTML = marked.parse(content);
  }

  wrapper.appendChild(avatar);
  wrapper.appendChild(bubble);
  container.appendChild(wrapper);
  container.scrollTop = container.scrollHeight;

  return bubble;
}


function updateStreamingBubble(text) {
  const bubble = document.getElementById("streaming-bubble");
  if (bubble) {
    // Render markdown on the accumulated text
    bubble.innerHTML = marked.parse(text);
    const container = document.getElementById("chatMessages");
    container.scrollTop = container.scrollHeight;
  }
}


function finaliseStreamingBubble(fullText) {
  const wrapper = document.getElementById("streaming-msg");
  const bubble  = document.getElementById("streaming-bubble");
  if (wrapper) wrapper.id = "";
  if (bubble)  bubble.id  = "";

  // Check if Claude signalled Excel is ready
  if (fullText.includes("[EXCEL_READY]")) {
    excelReady = true;
    // Remove the token from displayed text
    const cleanText = fullText.replace("[EXCEL_READY]", "").trim();
    if (bubble) bubble.innerHTML = marked.parse(cleanText);
    showExcelBanner(bubble);
    enableDownloadButton();
  }
}


function showExcelBanner(afterElement) {
  const banner = document.createElement("div");
  banner.className = "excel-banner mt-2";
  banner.innerHTML = `
    <span class="excel-icon">📊</span>
    <div class="flex-grow-1">
      <div class="fw-semibold text-success small">Excel report ready</div>
      <div class="text-muted" style="font-size:.75rem">
        All cost categories included — food, packaging, toys, marketing
      </div>
    </div>
    <a href="/export/excel" class="btn btn-success btn-sm" target="_blank">
      <i class="bi bi-download me-1"></i>Download
    </a>
  `;
  if (afterElement && afterElement.parentNode) {
    afterElement.parentNode.insertAdjacentElement("afterend", banner);
  }
  document.getElementById("chatMessages").scrollTop = 9999;
}


function enableDownloadButton() {
  const btn  = document.getElementById("downloadBtn");
  const hint = document.getElementById("exportHint");
  if (btn)  btn.style.display  = "block";
  if (hint) hint.style.display = "none";
}


// ── Send message ──────────────────────────────────────────────────────────

function sendMessage() {
  if (isStreaming) return;

  const input = document.getElementById("userInput");
  const text  = input.value.trim();
  if (!text) return;

  // Render user message
  appendMessage("user", text);

  // Add to history
  conversationHistory.push({ role: "user", content: text });
  input.value = "";
  input.style.height = "auto";

  setStatus("Thinking...");
  setLoading(true);

  // Render assistant streaming placeholder
  appendMessage("assistant", "", true);

  // Stream from backend
  let   accumulated = "";
  const payload     = JSON.stringify({ messages: conversationHistory });

  fetch("/chat/stream", {
    method:  "POST",
    headers: { "Content-Type": "application/json" },
    body:    payload
  })
  .then(response => {
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    const reader  = response.body.getReader();
    const decoder = new TextDecoder();

    function pump() {
      reader.read().then(({ done, value }) => {
        if (done) {
          finaliseStreamingBubble(accumulated);
          conversationHistory.push({ role: "assistant", content: accumulated });
          setLoading(false);
          setStatus("");
          return;
        }

        const chunk = decoder.decode(value, { stream: true });
        const lines = chunk.split("\n");

        for (const line of lines) {
          if (!line.startsWith("data: ")) continue;
          const token = line.slice(6);

          if (token === "[DONE]") {
            finaliseStreamingBubble(accumulated);
            conversationHistory.push({ role: "assistant", content: accumulated });
            setLoading(false);
            setStatus("");
            return;
          }
          if (token === "[AUTH_REQUIRED]" || token === "[SESSION_EXPIRED]") {
            window.location.href = "/auth/login";
            return;
          }
          if (token.startsWith("[ERROR]")) {
            updateStreamingBubble(`⚠️ ${token}`);
            setLoading(false);
            setStatus("");
            return;
          }
          if (token === "[NO_MESSAGE]") return;

          // Unescape newlines
          accumulated += token.replace(/\\n/g, "\n");
          updateStreamingBubble(accumulated);
        }

        pump();
      }).catch(err => {
        console.error("Stream read error:", err);
        setLoading(false);
        setStatus("Connection error");
      });
    }

    pump();
  })
  .catch(err => {
    console.error("Fetch error:", err);
    updateStreamingBubble("⚠️ Network error — please try again.");
    setLoading(false);
    setStatus("");
  });
}


// ── Utility ───────────────────────────────────────────────────────────────

function handleKey(event) {
  if (event.key === "Enter" && !event.shiftKey) {
    event.preventDefault();
    sendMessage();
  }
}

function setLoading(on) {
  isStreaming = on;
  const btn = document.getElementById("sendBtn");
  if (btn) {
    btn.disabled   = on;
    btn.innerHTML  = on
      ? '<span class="spinner-border spinner-border-sm"></span>'
      : '<i class="bi bi-send-fill"></i>';
  }
}

function setStatus(text) {
  const el = document.getElementById("statusText");
  if (el) el.textContent = text;
}

function newChat() {
  conversationHistory.length = 0;
  excelReady = false;
  document.getElementById("chatMessages").innerHTML = `
    <div class="message assistant-message">
      <div class="message-avatar"><i class="bi bi-robot"></i></div>
      <div class="message-bubble">
        <p class="mb-0">New conversation started. How can I help you with CostIQ data?</p>
      </div>
    </div>`;
  document.getElementById("downloadBtn").style.display = "none";
  document.getElementById("exportHint").style.display  = "block";
}

function downloadExcel() {
  window.open("/export/excel", "_blank");
}

// ── Quick question buttons ─────────────────────────────────────────────────
document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".quick-btn").forEach(btn => {
    btn.addEventListener("click", () => {
      const input = document.getElementById("userInput");
      input.value = btn.dataset.q;
      sendMessage();
    });
  });

  // Auto-resize textarea
  const textarea = document.getElementById("userInput");
  textarea.addEventListener("input", function () {
    this.style.height = "auto";
    this.style.height = Math.min(this.scrollHeight, 150) + "px";
  });

  textarea.focus();
});
