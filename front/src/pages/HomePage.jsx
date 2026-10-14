import { useEffect, useState } from 'react';
import styled from 'styled-components';
import { getCompanies } from '../api/companyApi';
import { Card, Message } from '../components/FormElements';
import { PageSpinner } from '../components/Spinner';

const List = styled.ul`
  li {
    display: flex;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  }

  li:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
`;

const Code = styled.span`
  font-family: ui-monospace, Consolas, monospace;
  font-size: 14px;
  color: ${({ theme }) => theme.colors.muted};
`;

// 연결 확인용 화면. 종목 5개가 보이면 React → proxy → 백엔드 연결이 된 것이다.
function HomePage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getCompanies(1, 5)
      .then((res) => setItems(res.data.data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <PageSpinner />;
  if (error) return <Message $error>연결 실패: {error}</Message>;

  return (
    <Card as="section">
      <h2>연결 확인</h2>
      <List>
        {items.map((c) => (
          <li key={c.code}>
            <Code>{c.code}</Code> {c.name}
          </li>
        ))}
      </List>
    </Card>
  );
}

export default HomePage;
