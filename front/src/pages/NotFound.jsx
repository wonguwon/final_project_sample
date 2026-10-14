import { Link } from 'react-router-dom';
import styled from 'styled-components';

const Wrapper = styled.section`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 16px;
  text-align: center;

  strong {
    font-size: 64px;
    font-weight: 800;
    line-height: 1;
    color: ${({ theme }) => theme.colors.primary};
  }

  h2 {
    margin-bottom: 16px;
  }

  a {
    padding: 10px 20px;
    border-radius: ${({ theme }) => theme.radius};
    background: ${({ theme }) => theme.colors.primary};
    color: ${({ theme }) => theme.colors.white};
    font-weight: 700;
    transition: background 0.15s;
  }

  a:hover {
    background: ${({ theme }) => theme.colors.primaryHover};
  }
`;

function NotFound() {
  return (
    <Wrapper>
      <strong>404</strong>
      <h2>페이지를 찾을 수 없습니다</h2>
      <Link to="/">홈으로 가기</Link>
    </Wrapper>
  );
}

export default NotFound;
