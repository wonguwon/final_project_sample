import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { logout } from '../api/authApi';
import useAuthStore from '../stores/useAuthStore';

const Header = styled.header`
  background: ${({ theme }) => theme.colors.white};
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};
`;

const Nav = styled.nav`
  max-width: ${({ theme }) => theme.maxWidth};
  margin: 0 auto;
  padding: 16px;
  display: flex;
  gap: 24px;

  /* NavLink 는 현재 주소와 맞으면 active 클래스를 붙여 준다 */
  a.active {
    color: ${({ theme }) => theme.colors.primary};
    font-weight: 700;
  }
`;

// 오른쪽 끝으로 미는 로그인 영역
const AuthMenu = styled.div`
  margin-left: auto;
  display: flex;
  gap: 16px;
  align-items: center;
`;

const TextButton = styled.button`
  border: none;
  background: none;
  color: ${({ theme }) => theme.colors.muted};
`;

const Main = styled.main`
  max-width: ${({ theme }) => theme.maxWidth};
  margin: 0 auto;
  padding: 24px 16px;
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
          <NavLink to="/search">뉴스 검색</NavLink>
          <NavLink to="/predict">예측</NavLink>

          <AuthMenu>
            {member ? (
              <>
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
