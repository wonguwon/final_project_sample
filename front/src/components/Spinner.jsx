import { ClipLoader } from 'react-spinners';
import styled, { useTheme } from 'styled-components';

const Center = styled.div`
  display: flex;
  justify-content: center;
  padding: 40px 0;
`;

// 화면(또는 한 영역)을 불러오는 동안 가운데에 보여 주는 로딩 표시
export function PageSpinner() {
  const theme = useTheme(); // styled-components 밖(일반 컴포넌트)에서 theme 을 꺼내는 방법
  return (
    <Center role="status" aria-label="불러오는 중">
      <ClipLoader color={theme.colors.primary} size={36} />
    </Center>
  );
}

// 버튼 안에 넣는 작은 로딩 표시 — 글자색(currentColor)을 그대로 따라간다
export function ButtonSpinner() {
  return <ClipLoader color="currentColor" size={14} />;
}
