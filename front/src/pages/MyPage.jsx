import { useEffect, useState } from 'react';
import styled from 'styled-components';
import { getMe } from '../api/memberApi';
import { sendTestNotification } from '../api/notificationApi';
import { Card, Message, SubButton } from '../components/FormElements';
import { PageSpinner } from '../components/Spinner';

const Box = styled(Card).attrs({ as: 'section' })`
  max-width: 480px;
  margin: 0 auto;
`;

// 항목 이름(dt) · 값(dd) 을 한 줄씩
const InfoList = styled.dl`
  display: grid;
  grid-template-columns: 100px 1fr;

  dt,
  dd {
    padding: 14px 0;
    border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  }

  dt {
    font-size: 14px;
    color: ${({ theme }) => theme.colors.muted};
  }

  dd {
    font-weight: 600;
  }

  dt:last-of-type,
  dd:last-of-type {
    border-bottom: none;
  }
`;

const TestBar = styled.div`
  margin-top: 20px;
  text-align: right;
`;

// 로그인한 사람만 들어올 수 있다 (App.jsx 에서 RequireAuth 로 감쌌다)
function MyPage() {
  const [me, setMe] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    // access token 이 만료되어 있어도 client 가 알아서 재발급 후 다시 요청한다
    getMe()
      .then((res) => setMe(res.data.data))
      .catch((err) => setError(err.response?.data?.error?.message ?? err.message));
  }, []);

  if (error) return <Message $error>불러오지 못했습니다: {error}</Message>;
  if (!me) return <PageSpinner />;

  return (
    <Box>
      <h2>내 정보</h2>
      <InfoList>
        <dt>회원 번호</dt>
        <dd>{me.memberId}</dd>
        <dt>아이디</dt>
        <dd>{me.loginId}</dd>
        <dt>닉네임</dt>
        <dd>{me.nickname}</dd>
      </InfoList>

      {/* 확인용 — 발표 전에 지운다 */}
      <TestBar>
        <SubButton onClick={() => sendTestNotification()}>테스트 알림 보내기</SubButton>
      </TestBar>
    </Box>
  );
}

export default MyPage;
