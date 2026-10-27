import { useEffect, useRef, useState } from 'react';
import styled from 'styled-components';
import { askChatbot, clearChatbotHistory, getChatbotHistory } from '../api/chatbotApi';
import { Button, Input, Message, SubButton } from '../components/FormElements';
import { ButtonSpinner } from '../components/Spinner';
import { errorMessage } from '../utils/format';

const Header = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  button {
    margin-left: auto;
  }
`;

const Board = styled.ul`
  height: 55vh;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: ${({ theme }) => theme.colors.white};
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radiusLg};

  > p {
    margin: auto;
    font-size: 14px;
    color: ${({ theme }) => theme.colors.muted};
  }
`;

// 내 질문(USER)은 오른쪽, 챗봇 답변(ASSISTANT)은 왼쪽
const Bubble = styled.li`
  max-width: 75%;
  align-self: ${({ $user }) => ($user ? 'flex-end' : 'flex-start')};
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  white-space: pre-wrap; /* 줄바꿈을 살린다 */
  word-break: break-word;
  color: ${({ $user, theme }) => ($user ? theme.colors.white : theme.colors.text)};
  background: ${({ $user, theme }) => ($user ? theme.colors.primary : theme.colors.background)};
  opacity: ${({ $pending }) => ($pending ? 0.6 : 1)};
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

const Notice = styled.p`
  margin-top: 8px;
  font-size: 13px;
  color: ${({ theme }) => theme.colors.muted};
`;

// /chatbot — 서비스 안내 챗봇 (로그인한 사람만)
function ChatbotPage() {
  const [messages, setMessages] = useState([]); // { role: 'USER' | 'ASSISTANT', text }
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const [remaining, setRemaining] = useState(null);
  const [error, setError] = useState(null);
  const boardRef = useRef(null);

  // 새로고침해도 이어서 보이게 — 서버가 기억하는 대화를 불러온다
  useEffect(() => {
    getChatbotHistory()
      .then((res) => setMessages(res.data.data))
      .catch((err) => setError(errorMessage(err)));
  }, []);

  useEffect(() => {
    boardRef.current?.scrollTo({ top: boardRef.current.scrollHeight });
  }, [messages, sending]);

  const send = async (e) => {
    e.preventDefault();
    const question = text.trim();
    if (!question || sending) return; // 답을 기다리는 동안은 보내지 않는다

    setMessages((prev) => [...prev, { role: 'USER', text: question }]); // 나 혼자 보는 대화라 질문은 바로 보여 준다
    setText('');
    setSending(true);
    setError(null);

    try {
      const res = await askChatbot(question);
      setMessages((prev) => [...prev, { role: 'ASSISTANT', text: res.data.data.reply }]);
      setRemaining(res.data.data.remaining);
    } catch (err) {
      setError(errorMessage(err)); // "오늘 질문 횟수를 모두 썼습니다." 등 서버 메시지 그대로
    } finally {
      setSending(false);
    }
  };

  const reset = async () => {
    await clearChatbotHistory();
    setMessages([]);
    setError(null);
  };

  return (
    <section>
      <Header>
        <h2>안내 챗봇</h2>
        <SubButton onClick={reset} disabled={sending || messages.length === 0}>
          새 대화
        </SubButton>
      </Header>

      {error && <Message $error>{error}</Message>}

      <Board ref={boardRef}>
        {messages.length === 0 && !sending && <p>서비스 사용법, 화면에 나온 결과의 뜻, 주식 용어를 물어보세요.</p>}
        {messages.map((m, i) => (
          // AI 답변도 바깥에서 온 글이므로 글자로만 그린다
          <Bubble key={i} $user={m.role === 'USER'}>
            {m.text}
          </Bubble>
        ))}
        {sending && <Bubble $pending>답변을 만드는 중…</Bubble>}
      </Board>

      <SendForm onSubmit={send}>
        <Input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="질문을 입력하세요 (500자까지)"
          maxLength={500}
          disabled={sending}
        />
        <Button type="submit" disabled={sending || !text.trim()}>
          {sending ? <ButtonSpinner /> : '전송'}
        </Button>
      </SendForm>

      <Notice>
        개인정보(주민번호 · 계좌번호 등)를 입력하지 마세요. AI 답변은 틀릴 수 있으며 참고용입니다.
        {remaining !== null && ` · 오늘 남은 질문 ${remaining}회`}
      </Notice>
    </section>
  );
}

export default ChatbotPage;
