import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { logout } from '../api/authApi';
import useAuthStore from '../stores/useAuthStore';
import NotificationBell from './NotificationBell';

const Header = styled.header`
  position: sticky;
  top: 0;
  z-index: 10;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
`;

const Nav = styled.nav`
  max-width: ${({ theme }) => theme.maxWidth};
  margin: 0 auto;
  /* 링크 안쪽 여백(12px)만큼 덜 띄워서 메뉴 글자를 본문 왼쪽 끝에 맞춘다 */
  padding: 0 calc(${({ theme }) => theme.gutter} - 12px);
  min-height: 60px;
  display: flex;
  flex-wrap: wrap; /* 화면이 좁으면 로그인 영역이 다음 줄로 내려간다 */
  align-items: center;
  gap: 4px;

  a {
    padding: 8px 12px;
    border-radius: ${({ theme }) => theme.radius};
    font-size: 15px;
    font-weight: 500;
    color: ${({ theme }) => theme.colors.muted};
    white-space: nowrap;
    transition:
      color 0.15s,
      background 0.15s;
  }

  a:hover {
    color: ${({ theme }) => theme.colors.text};
    background: ${({ theme }) => theme.colors.background};
  }

  /* NavLink 는 현재 주소와 맞으면 active 클래스를 붙여 준다 */
  a.active {
    color: ${({ theme }) => theme.colors.primary};
    background: ${({ theme }) => theme.colors.primarySoft};
    font-weight: 700;
  }

  @media (max-width: 600px) {
    padding: 8px 6px; /* 링크 안쪽 여백(10px)과 합쳐 16px */
    gap: 2px;

    a {
      padding: 6px 10px;
      font-size: 14px;
    }
  }
`;

// 오른쪽 끝으로 미는 로그인 영역
const AuthMenu = styled.div`
  margin-left: auto;
  display: flex;
  gap: 4px;
  align-items: center;
`;

const TextButton = styled.button`
  padding: 8px 12px;
  border: none;
  border-radius: ${({ theme }) => theme.radius};
  background: none;
  font-size: 15px;
  color: ${({ theme }) => theme.colors.muted};
  white-space: nowrap;
  transition:
    color 0.15s,
    background 0.15s;

  &:hover {
    color: ${({ theme }) => theme.colors.text};
    background: ${({ theme }) => theme.colors.background};
  }

  @media (max-width: 600px) {
    padding: 6px 10px;
    font-size: 14px;
  }
`;

const Main = styled.main`
  max-width: ${({ theme }) => theme.maxWidth};
  margin: 0 auto;
  padding: 32px ${({ theme }) => theme.gutter} 64px;

  @media (max-width: 600px) {
    padding: 20px 16px 48px; /* 위 메뉴의 모바일 여백(16px)과 왼쪽 끝을 맞춘다 */
  }
`;

function Layout() {
  const member = useAuthStore((s) => s.member);
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <>
      <Header>
        <Nav>
          <NavLink to="/" end>
            홈
          </NavLink>
          <NavLink to="/companies">종목</NavLink>
          <NavLink to="/posts">토론</NavLink>
          <NavLink to="/search">뉴스 검색</NavLink>
          <NavLink to="/predict">예측</NavLink>
          <NavLink to="/chat">채팅</NavLink>
          <NavLink to="/chatbot">챗봇</NavLink>

          <AuthMenu>
            {member ? (
              <>
                <NotificationBell />
                <NavLink to="/mypage">{member.nickname} 님</NavLink>
                <TextButton onClick={handleLogout}>로그아웃</TextButton>
              </>
            ) : (
              <>
                <NavLink to="/login">로그인</NavLink>
                <NavLink to="/signup">회원가입</NavLink>
              </>
            )}
          </AuthMenu>
        </Nav>
      </Header>
      <Main>
        <Outlet />
      </Main>
    </>
  );
}

export default Layout;
