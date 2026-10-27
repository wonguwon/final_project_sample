import { useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import styled from 'styled-components';
import { getMessages } from '../api/chatApi';
import { createStompClient } from '../chat/stompClient';
import { Button, Input, Message } from '../components/FormElements';
import useAuthStore from '../stores/useAuthStore';
import { errorMessage, formatDateTime } from '../utils/format';

const Header = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  a {
    font-size: 14px;
    color: ${({ theme }) => theme.colors.muted};
  }
`;

const Status = styled.span`
  margin-left: auto;
  font-size: 13px;
  color: ${({ $ok, theme }) => ($ok ? theme.colors.primary : theme.colors.danger)};
`;

const Board = styled.ul`
  height: 60vh;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: ${({ theme }) => theme.colors.white};
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radiusLg};
`;

// 내 메시지는 오른쪽, 다른 사람은 왼쪽
const Bubble = styled.li`
  max-width: 70%;
  align-self: ${({ $mine }) => ($mine ? 'flex-end' : 'flex-start')};
  text-align: ${({ $mine }) => ($mine ? 'right' : 'left')};

  small {
    display: block;
    margin-bottom: 2px;
    font-size: 12px;
    color: ${({ theme }) => theme.colors.muted};
  }

  p {
    display: inline-block;
    padding: 8px 12px;
    border-radius: 12px;
    text-align: left;
    white-space: pre-wrap;
    word-break: break-word;
    color: ${({ $mine, theme }) => ($mine ? theme.colors.white : theme.colors.text)};
    background: ${({ $mine, theme }) => ($mine ? theme.colors.primary : theme.colors.background)};
  }
`;

const SendForm = styled.form`
  display: flex;
  gap: 8px;
  margin-top: 12px;

  button {
    min-width: 80px;
    padding: 10px 18px;
  }
`;

// 같은 메시지가 두 번 들어와도 한 번만, 번호 순서대로
const mergeMessages = (prev, incoming) => {
  const map = new Map(prev.map((m) => [m.messageId, m]));
  incoming.forEach((m) => map.set(m.messageId, m));
  return [...map.values()].sort((a, b) => a.messageId - b.messageId);
};

// /chat/:roomId — 지난 메시지(REST) + 실시간 대화(WebSocket)
function ChatRoomPage() {
  const { roomId } = useParams();
  const me = useAuthStore((s) => s.member);

  const [messages, setMessages] = useState([]);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState(null);
  const [text, setText] = useState('');
  const clientRef = useRef(null);
  const boardRef = useRef(null);

  useEffect(() => {
    // 1) 지난 메시지 (REST)
    getMessages(roomId)
      .then((res) => setMessages((prev) => mergeMessages(prev, res.data.data)))
      .catch((err) => setError(errorMessage(err)));

    // 2) 실시간 메시지 (WebSocket)
    const client = createStompClient();
    client.onConnect = () => {
      setConnected(true);
      client.subscribe(`/topic/chat/rooms/${roomId}`, (frame) => {
        setMessages((prev) => mergeMessages(prev, [JSON.parse(frame.body)]));
      });
      client.subscribe('/user/queue/errors', (frame) => {
        setError(JSON.parse(frame.body).error.message);
      });
    };
    client.onWebSocketClose = () => setConnected(false);
    client.activate();
    clientRef.current = client;

    // 화면을 떠나거나 방이 바뀌면 연결을 끊는다
    return () => {
      client.deactivate();
      setMessages([]);
    };
  }, [roomId]);

  // 새 메시지가 오면 맨 아래로 스크롤
  useEffect(() => {
    boardRef.current?.scrollTo({ top: boardRef.current.scrollHeight });
  }, [messages]);

  const send = (e) => {
    e.preventDefault();
    if (!text.trim() || !connected) return;
    clientRef.current.publish({
      destination: `/app/chat/rooms/${roomId}`,
      body: JSON.stringify({ content: text }),
    });
    setText(''); // 화면에는 그리지 않는다. 서버가 돌려준 MESSAGE 가 오면 그때 그린다
    setError(null);
  };

  return (
    <section>
      <Header>
        <Link to="/chat">← 채팅방 목록</Link>
        <Status $ok={connected}>{connected ? '● 연결됨' : '연결이 끊겼습니다. 다시 연결하는 중…'}</Status>
      </Header>

      {error && <Message $error>{error}</Message>}

      <Board ref={boardRef}>
        {messages.map((m) => {
          const mine = m.senderId === me?.memberId;
          return (
            <Bubble key={m.messageId} $mine={mine}>
              <small>
                {mine ? '나' : m.senderName} · {formatDateTime(m.createdAt).slice(11)}
              </small>
              {/* 글자로만 그린다 — 다른 사람이 보낸 <script> 가 실행되지 않게 */}
              <p>{m.content}</p>
            </Bubble>
          );
        })}
      </Board>

      <SendForm onSubmit={send}>
        <Input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder={connected ? '메시지를 입력하세요' : '연결 중…'}
          maxLength={1000}
          disabled={!connected}
        />
        <Button type="submit" disabled={!connected || !text.trim()}>
          전송
        </Button>
      </SendForm>
    </section>
  );
}

export default ChatRoomPage;
