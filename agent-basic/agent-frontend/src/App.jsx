import React, { useState, useRef, useEffect } from 'react';

function App() {
  const [messages, setMessages] = useState([
    { sender: 'agent', text: 'Hello! I am your AI agent equipped with tools and memory. Ask me anything!' }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [conversationId] = useState('react-session-1');
  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const sendMessage = async (e) => {
    e.preventDefault();
    if (!input.trim() || loading) return;

    const userMessage = input.trim();
    setInput('');
    setMessages((prev) => [...prev, { sender: 'user', text: userMessage }]);
    setLoading(true);

    try {
      const response = await fetch(
        `http://localhost:8080/chat?message=${encodeURIComponent(userMessage)}&conversationId=${conversationId}`
      );

      if (!response.ok) throw new Error('Backend failed to respond');

      const reply = await response.text();
      setMessages((prev) => [...prev, { sender: 'agent', text: reply }]);
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        { sender: 'agent', text: 'Error connecting to Spring AI backend. Check if it is running on port 8080.' }
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h2 style={{ margin: 0 }}>Spring AI Agent Console</h2>
        <span style={styles.badge}>Session: {conversationId}</span>
      </header>

      <div style={styles.chatBox}>
        {messages.map((msg, index) => (
          <div
            key={index}
            style={{
              ...styles.messageRow,
              justifyContent: msg.sender === 'user' ? 'flex-end' : 'flex-start'
            }}
          >
            <div
              style={{
                ...styles.bubble,
                backgroundColor: msg.sender === 'user' ? '#2563eb' : '#f1f5f9',
                color: msg.sender === 'user' ? '#ffffff' : '#0f172a'
              }}
            >
              {msg.text}
            </div>
          </div>
        ))}

        {loading && (
          <div style={{ ...styles.messageRow, justifyContent: 'flex-start' }}>
            <div style={{ ...styles.bubble, backgroundColor: '#f1f5f9', color: '#64748b' }}>
              Agent is thinking or executing tools...
            </div>
          </div>
        )}
        <div ref={chatEndRef} />
      </div>

      <form onSubmit={sendMessage} style={styles.form}>
        <input
          type="text"
          placeholder="Ask a question or request a conversion..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          style={styles.input}
        />
        <button type="submit" disabled={loading} style={styles.button}>
          Send
        </button>
      </form>
    </div>
  );
}

const styles = {
  container: { display: 'flex', flexDirection: 'column', height: '100vh', maxWidth: '750px', margin: '0 auto', fontFamily: 'system-ui, sans-serif' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px', borderBottom: '1px solid #e2e8f0' },
  badge: { fontSize: '12px', background: '#e0f2fe', color: '#0369a1', padding: '4px 8px', borderRadius: '12px' },
  chatBox: { flex: 1, padding: '16px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '10px' },
  messageRow: { display: 'flex', width: '100%' },
  bubble: { maxWidth: '75%', padding: '12px 16px', borderRadius: '12px', lineHeight: '1.4', whiteSpace: 'pre-wrap' },
  form: { display: 'flex', padding: '16px', gap: '10px', borderTop: '1px solid #e2e8f0' },
  input: { flex: 1, padding: '12px', borderRadius: '8px', border: '1px solid #cbd5e1', outline: 'none', fontSize: '14px' },
  button: { padding: '12px 24px', backgroundColor: '#2563eb', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }
};

export default App;