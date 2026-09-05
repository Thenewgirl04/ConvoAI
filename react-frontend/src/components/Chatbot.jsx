import { useState, useEffect, useRef, useCallback } from "react";
import axios from "axios";

const STORAGE_KEY = "chat.sessionId";
const MODE_STORAGE_KEY = "chat.mode";
const CLIENT_ID_KEY = "chat.clientId";
const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const toSender = (role) => (role === "user" ? "user" : "bot");

const getClientId = () => {
  try {
    let id = localStorage.getItem(CLIENT_ID_KEY);
    if (!id) {
      id = crypto.randomUUID();
      localStorage.setItem(CLIENT_ID_KEY, id);
    }
    return id;
  } catch {
    return crypto.randomUUID();
  }
};

const formatRelativeTime = (isoString) => {
  if (!isoString) return "";
  const date = new Date(isoString);
  const diffMs = Date.now() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  if (diffMins < 1) return "Just now";
  if (diffMins < 60) return `${diffMins}m ago`;
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours}h ago`;
  return date.toLocaleDateString();
};

const ChatBot = () => {
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState([]);
  const [sessionId, setSessionId] = useState(null);
  const [therapy, setTherapy] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [sessions, setSessions] = useState([]);
  const [loadingSessions, setLoadingSessions] = useState(false);
  const clientIdRef = useRef(getClientId());
  const chatEndRef = useRef(null);

  const fetchSessions = useCallback(async () => {
    setLoadingSessions(true);
    try {
      const res = await axios.get(`${API_BASE}/sessions`, {
        params: { clientId: clientIdRef.current },
      });
      setSessions(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingSessions(false);
    }
  }, []);

  const loadSession = useCallback(async (sid) => {
    if (!sid) return;

    setLoadingHistory(true);
    try {
      const res = await axios.get(`${API_BASE}/chat/${sid}`, {
        params: { clientId: clientIdRef.current },
      });
      const { sessionId: id, mode, messages: history } = res.data || {};

      setSessionId(id ?? sid);
      if (mode === "therapy") setTherapy(true);
      else if (mode === "default") setTherapy(false);

      setMessages(
        Array.isArray(history)
          ? history.map((msg) => ({
              sender: toSender(msg.role),
              text: msg.content,
            }))
          : []
      );
    } catch (err) {
      if (err.response?.status === 404) {
        setSessionId(null);
        setMessages([]);
        try {
          localStorage.removeItem(STORAGE_KEY);
        } catch {
          /* ignore */
        }
      }
      console.error(err);
    } finally {
      setLoadingHistory(false);
    }
  }, []);

  useEffect(() => {
    const init = async () => {
      let savedSessionId = null;
      try {
        savedSessionId = localStorage.getItem(STORAGE_KEY);
        const savedMode = localStorage.getItem(MODE_STORAGE_KEY);
        if (savedMode === "therapy") setTherapy(true);
      } catch {
        /* ignore */
      }

      await fetchSessions();

      if (savedSessionId) {
        await loadSession(savedSessionId);
      }
    };

    init();
  }, [fetchSessions, loadSession]);

  useEffect(() => {
    try {
      if (sessionId) localStorage.setItem(STORAGE_KEY, sessionId);
      else localStorage.removeItem(STORAGE_KEY);
      localStorage.setItem(MODE_STORAGE_KEY, therapy ? "therapy" : "default");
    } catch {
      /* ignore */
    }
  }, [sessionId, therapy]);

  const push = (sender, text) =>
    setMessages((prev) => [...prev, { sender, text }]);

  const sendMessage = async () => {
    if (!input.trim() || loadingHistory) return;

    const text = input.trim();
    setInput("");
    push("user", text);

    try {
      const body = {
        prompt: text,
        mode: therapy ? "therapy" : "default",
        sessionId: sessionId || undefined,
        clientId: clientIdRef.current,
      };
      const res = await axios.post(`${API_BASE}/chat`, body, {
        headers: { "Content-Type": "application/json" },
      });

      const { sessionId: sid, reply, mode } = res.data || {};
      if (sid && sid !== sessionId) setSessionId(sid);
      if (mode === "therapy") setTherapy(true);
      else if (mode === "default") setTherapy(false);

      push("bot", reply ?? "(no content)");
      await fetchSessions();
    } catch (err) {
      console.error(err);
      push("bot", "Error retrieving response");
    }
  };

  const newSession = () => {
    setSessionId(null);
    setMessages([]);
  };

  const handleModeToggle = (nextTherapy) => {
    if (nextTherapy === therapy) return;
    setTherapy(nextTherapy);
    setSessionId(null);
    setMessages([]);
  };

  const selectSession = async (sid) => {
    if (sid === sessionId) return;
    await loadSession(sid);
  };

  const deleteSession = async (sid, event) => {
    event.stopPropagation();
    if (!window.confirm("Delete this conversation?")) return;

    try {
      await axios.delete(`${API_BASE}/sessions/${sid}`, {
        params: { clientId: clientIdRef.current },
      });
      if (sid === sessionId) {
        setSessionId(null);
        setMessages([]);
      }
      await fetchSessions();
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  return (
      <>
        <h2 className="app-heading">CONVOAI</h2>
    <div className="page-shell">
      <aside className="session-sidebar">
        <div className="session-sidebar-header">
          <h5 className="m-0">Conversations</h5>
          <button
            className="btn btn-sm btn-new-chat"
            onClick={newSession}
            disabled={loadingHistory}
          >
            + New
          </button>
        </div>

        <div className="session-list">
          {loadingSessions && sessions.length === 0 ? (
            <div className="session-empty">Loading...</div>
          ) : sessions.length === 0 ? (
            <div className="session-empty">No conversations yet</div>
          ) : (
            sessions.map((session) => (
              <button
                key={session.sessionId}
                type="button"
                className={`session-item ${session.sessionId === sessionId ? "active" : ""}`}
                onClick={() => selectSession(session.sessionId)}
              >
                <span className="session-item-title">
                  {session.title || "New conversation"}
                </span>
                <span className="session-item-meta">
                  {session.mode === "therapy" ? "Therapy" : "General"} ·{" "}
                  {formatRelativeTime(session.lastActiveAt)}
                </span>
                <span
                  className="session-item-delete"
                  role="button"
                  tabIndex={0}
                  aria-label="Delete conversation"
                  onClick={(e) => deleteSession(session.sessionId, e)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") deleteSession(session.sessionId, e);
                  }}
                >
                  ×
                </span>
              </button>
            ))
          )}
        </div>
      </aside>

      <div className="card chat-card shadow-lg">
        <div className="card-header custom-header d-flex justify-content-between align-items-center">
          <h4 className="m-0">Personal Assistant</h4>
          <div className="d-flex align-items-center gap-3">
            <div className="form-check form-switch m-0">
              <input
                className="form-check-input"
                type="checkbox"
                id="therapySwitch"
                checked={therapy}
                disabled={loadingHistory}
                onChange={(e) => handleModeToggle(e.target.checked)}
              />
              <label className="form-check-label" htmlFor="therapySwitch">
                Therapy Mode
              </label>
            </div>
          </div>
        </div>

        <div className="card-body chat-box">
          {loadingHistory ? (
            <div className="placeholder-message">Loading conversation...</div>
          ) : messages.length === 0 ? (
            <div className="placeholder-message">
              No messages yet 👋
              {therapy && (
                <div className="mt-2 small text-muted">
                  Therapy mode — conversations are separate from general chat.
                </div>
              )}
            </div>
          ) : (
            messages.map((msg, i) => (
              <div
                key={i}
                className={`message ${msg.sender === "user" ? "user" : "bot"}`}
              >
                {msg.text}
              </div>
            ))
          )}
          <div ref={chatEndRef} />
        </div>

        <div className="card-footer">
          <div className="input-group">
            <input
              type="text"
              className="form-control"
              placeholder="Type your message..."
              value={input}
              disabled={loadingHistory}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && sendMessage()}
            />
            <button
              className="btn btn-primary"
              onClick={sendMessage}
              disabled={loadingHistory}
            >
              Send
            </button>
          </div>
        </div>
      </div>
    </div>
      </>
  );
};

export default ChatBot;
