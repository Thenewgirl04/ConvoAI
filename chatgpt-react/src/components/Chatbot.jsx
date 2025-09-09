import { useState, useEffect, useRef } from "react";
import axios from "axios";
import "bootstrap/dist/css/bootstrap.min.css";
              

const STORAGE_KEY = "chat.sessionId";

const ChatBot = () => {
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState([]); 
  const [sessionId, setSessionId] = useState(null);  
  const [therapy, setTherapy] = useState(false);
  const chatEndRef = useRef(null);

    useEffect(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      if (saved) setSessionId(saved);
    } catch {
      /* ignore if storage blocked */
    }
  }, []);

  // Anytime sessionId changes, persist it
  useEffect(() => {
    try {
      if (sessionId) localStorage.setItem(STORAGE_KEY, sessionId);
      else localStorage.removeItem(STORAGE_KEY);
    } catch {
      /* ignore if storage blocked */
    }
      }, [sessionId]);


  const push = (sender, text) => 
    setMessages(prev => [...prev, { sender,text}]);
  
  const sendMessage = async () => {
    if (!input.trim()) return; 

    const text = input.trim();
    setInput("");
    push("user", text);

    try {

        const body = {
        prompt: text,
        mode: therapy ? "therapy" : "default",
        sessionId: sessionId || undefined, // backend makes one if undefined
      };
      const res = await axios.post(
        "http://localhost:8080/api/chat", body, 
        { headers: { "Content-Type": "application/json" }, }
      );

      const { sessionId: sid, reply} = res.data || {};
      if (sid && sid !== sessionId) setSessionId(sid);
      
      push("bot", reply ?? "(no content)");
    } catch (err) {
      console.error(err);
      push("bot", "Error retrieving response");
    }
  };

  const newSession = () => {
    setSessionId(null);     // next send will get a fresh sessionId
    setMessages([]);
  };

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  return (
  <div className="page-shell">
    <div className="card chat-card shadow-lg">
      {/* header */}
      <div className="card-header custom-header d-flex justify-content-between align-items-center">
        <h4 className="m-0">Personal Assistant</h4>
        <div className="d-flex align-items-center gap-3">
          <small className="me-2">session: {sessionId ?? "— new —"}</small>
          <div className="form-check form-switch m-0">
            <input
              className="form-check-input"
              type="checkbox"
              id="therapySwitch"
              checked={therapy}
              onChange={(e) => setTherapy(e.target.checked)}
            />
            <label className="form-check-label" htmlFor="therapySwitch">
              Therapy Mode
            </label>
          </div>
          <button className="btn btn-outline-light btn-sm" onClick={newSession}>
            New session
          </button>
        </div>
      </div>

      {/* messages */}
      <div className="card-body chat-box">
        {messages.length === 0 ? (
            <div className="placeholder-message">
            No messages yet 👋
            </div>
        ) : (
        messages.map((msg, i) => (
          <div key={i} className={`message ${msg.sender === "user" ? "user" : "bot"}`}>
            {msg.text}
          </div>
        ))
        )}
        <div ref={chatEndRef} />
      </div>

      {/* input pinned to bottom */}
      <div className="card-footer">
        <div className="input-group">
          <input
            type="text"
            className="form-control"
            placeholder="Type your message..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && sendMessage()}
          />
          <button className="btn btn-primary" onClick={sendMessage}>
            Send
          </button>
        </div>
      </div>
    </div>
  </div>
);
}

export default ChatBot;
