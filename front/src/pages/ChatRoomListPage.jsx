import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { createRoom, getRooms, joinRoom } from '../api/chatApi';
import { Button, Card, Input, Message, SubButton } from '../components/FormElements';
import { ButtonSpinner, PageSpinner } from '../components/Spinner';
import { errorMessage } from '../utils/format';

const CreateForm = styled.form`
  display: flex;
  gap: 8px;
  margin: 16px 0 24px;

  button {
    min-width: 110px;
    padding: 10px 18px;
  }
`;

const RoomList = styled(Card).attrs({ as: 'ul' })`
  padding: 0;

  li {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  }

  li:last-child {
    border-bottom: none;
  }

  strong {
    font-size: 16px;
  }

  span {
    font-size: 13px;
    color: ${({ theme }) => theme.colors.muted};
  }

  button {
    margin-left: auto;
  }
`;

// /chat — 방 목록, 방 만들기, 참여하기 (로그인한 사람만)
function ChatRoomListPage() {
  const navigate = useNavigate();
  const [rooms, setRooms] = useState(null);
  const [title, setTitle] = useState('');
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    getRooms()
      .then((res) => setRooms(res.data.data))
      .catch((err) => setError(errorMessage(err)));
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!title.trim() || creating) return;
    setCreating(true);
    try {
      const res = await createRoom(title.trim());
      navigate(`/chat/${res.data.data.roomId}`); // 만든 사람은 자동 참여 → 바로 입장
    } catch (err) {
      setError(errorMessage(err));
      setCreating(false);
    }
  };

  const handleJoin = async (roomId) => {
    try {
      await joinRoom(roomId);
      navigate(`/chat/${roomId}`);
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <section>
      <h2>채팅</h2>

      <CreateForm onSubmit={handleCreate}>
        <Input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="새 채팅방 이름" maxLength={100} />
        <Button type="submit" disabled={creating || !title.trim()}>
          {creating ? <ButtonSpinner /> : '방 만들기'}
        </Button>
      </CreateForm>

      {error && <Message $error>{error}</Message>}
      {!rooms && !error && <PageSpinner />}
      {rooms && rooms.length === 0 && <p>아직 채팅방이 없습니다. 첫 방을 만들어 보세요.</p>}

      {rooms && rooms.length > 0 && (
        <RoomList>
          {rooms.map((room) => (
            <li key={room.roomId}>
              <strong>{room.title}</strong>
              <span>{room.memberCount}명</span>
              {room.joined ? (
                <SubButton onClick={() => navigate(`/chat/${room.roomId}`)}>들어가기</SubButton>
              ) : (
                <SubButton onClick={() => handleJoin(room.roomId)}>참여하기</SubButton>
              )}
            </li>
          ))}
        </RoomList>
      )}
    </section>
  );
}

export default ChatRoomListPage;
