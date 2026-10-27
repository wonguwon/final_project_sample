import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { getNotifications, getUnreadCount, readAllNotifications, readNotification } from '../api/notificationApi';
import { subscribeNotifications } from '../notification/subscribeNotifications';
import useAuthStore from '../stores/useAuthStore';
import { formatDateTime } from '../utils/format';

const Wrapper = styled.div`
  position: relative;
`;

const BellButton = styled.button`
  position: relative;
  padding: 6px 10px;
  border: none;
  border-radius: ${({ theme }) => theme.radius};
  background: none;
  font-size: 18px;
  line-height: 1;

  &:hover {
    background: ${({ theme }) => theme.colors.background};
  }
`;

const Badge = styled.span`
  position: absolute;
  top: 0;
  right: 2px;
  min-width: 18px;
  padding: 1px 5px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors.white};
  background: ${({ theme }) => theme.colors.danger};
`;

const Panel = styled.div`
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 320px;
  max-height: 400px;
  overflow-y: auto;
  background: ${({ theme }) => theme.colors.white};
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radiusLg};
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);

  header {
    display: flex;
    justify-content: space-between;
    padding: 12px 16px;
    font-size: 14px;
    font-weight: 700;
    border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  }

  header button {
    border: none;
    background: none;
    font-size: 13px;
    color: ${({ theme }) => theme.colors.primary};
  }

  p {
    padding: 24px 16px;
    font-size: 14px;
    text-align: center;
    color: ${({ theme }) => theme.colors.muted};
  }
`;

const Item = styled.button`
  display: block;
  width: 100%;
  padding: 12px 16px;
  border: none;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  background: ${({ $unread, theme }) => ($unread ? theme.colors.primarySoft : theme.colors.white)};
  text-align: left;
  font-size: 14px;
  font-weight: ${({ $unread }) => ($unread ? 700 : 400)};
  color: ${({ theme }) => theme.colors.text};

  small {
    display: block;
    margin-top: 4px;
    font-size: 12px;
    font-weight: 400;
    color: ${({ theme }) => theme.colors.muted};
  }
`;

const Toast = styled.div`
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 20;
  padding: 14px 18px;
  border-radius: ${({ theme }) => theme.radiusLg};
  font-size: 14px;
  color: ${({ theme }) => theme.colors.white};
  background: ${({ theme }) => theme.colors.text};
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.2);
`;

// 헤더(Layout)의 🔔 알림 종. 모든 화면에서 알림을 받는다
function NotificationBell() {
  const isLoggedIn = useAuthStore((s) => !!s.accessToken);
  const navigate = useNavigate();

  const [items, setItems] = useState([]);
  const [unread, setUnread] = useState(0);
  const [toast, setToast] = useState(null);
  const [open, setOpen] = useState(false);

  // 의존성은 accessToken 이 아니라 isLoggedIn — 토큰이 재발급되어 값이 바뀔 때마다 다시 연결할 필요는 없다
  useEffect(() => {
    if (!isLoggedIn) return; // 로그인하지 않았으면 연결하지 않는다

    // 1) 접속하지 않은 동안 쌓인 알림 (REST)
    getNotifications().then((res) => setItems(res.data.data));
    getUnreadCount().then((res) => setUnread(res.data.data));

    // 2) 지금부터 오는 알림 (SSE)
    const stop = subscribeNotifications((n) => {
      // 목록(REST)과 실시간(SSE)으로 같은 알림을 받을 수 있으므로 번호로 중복을 거른다
      setItems((prev) => (prev.some((p) => p.notificationId === n.notificationId) ? prev : [n, ...prev]));
      setUnread((c) => c + 1);
      setToast(n);
      setTimeout(() => setToast(null), 3000);
    });

    return stop; // 로그아웃하면(isLoggedIn → false) 연결을 끊는다
  }, [isLoggedIn]);

  if (!isLoggedIn) return null;

  const handleClick = async (n) => {
    if (!n.readAt) {
      await readNotification(n.notificationId);
      setItems((prev) => prev.map((p) => (p.notificationId === n.notificationId ? { ...p, readAt: 'now' } : p)));
      setUnread((c) => Math.max(c - 1, 0));
    }
    setOpen(false);
    if (n.link?.startsWith('/')) navigate(n.link); // 우리 사이트 안의 주소만 이동
  };

  const handleReadAll = async () => {
    await readAllNotifications();
    setItems((prev) => prev.map((p) => (p.readAt ? p : { ...p, readAt: 'now' })));
    setUnread(0);
  };

  return (
    <Wrapper>
      <BellButton onClick={() => setOpen(!open)} aria-label={`알림 ${unread}개`}>
        🔔
        {unread > 0 && <Badge>{unread > 99 ? '99+' : unread}</Badge>}
      </BellButton>

      {open && (
        <Panel>
          <header>
            알림
            {unread > 0 && <button onClick={handleReadAll}>모두 읽음</button>}
          </header>
          {items.length === 0 && <p>알림이 없습니다.</p>}
          {items.map((n) => (
            <Item key={n.notificationId} $unread={!n.readAt} onClick={() => handleClick(n)}>
              {n.message}
              <small>{formatDateTime(n.createdAt)}</small>
            </Item>
          ))}
        </Panel>
      )}

      {toast && <Toast role="status">🔔 {toast.message}</Toast>}
    </Wrapper>
  );
}

export default NotificationBell;
