import styled, { css } from 'styled-components';

// 여러 화면이 함께 쓰는 카드 · 폼 스타일

// 흰 바탕 상자. as="section" / styled(Card).attrs({ as: 'article' }) 처럼 태그를 바꿔 쓴다
export const Card = styled.div`
  padding: 28px;
  background: ${({ theme }) => theme.colors.white};
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radiusLg};
  box-shadow: ${({ theme }) => theme.shadow};

  @media (max-width: 600px) {
    padding: 20px 16px;
  }
`;

// 로그인 · 회원가입 화면의 폼
export const Form = styled.form`
  max-width: 400px;
  margin: 24px auto;
  padding: 36px 32px 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: ${({ theme }) => theme.colors.white};
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radiusLg};
  box-shadow: ${({ theme }) => theme.shadow};

  h2 {
    margin-bottom: 8px;
    text-align: center;
  }

  button[type='submit'] {
    margin-top: 4px;
  }

  @media (max-width: 600px) {
    margin: 8px auto;
    padding: 28px 20px 24px;
  }
`;

export const Label = styled.label`
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: ${({ theme }) => theme.colors.muted};
`;

// 라벨 옆의 작은 안내 글
export const Hint = styled.span`
  font-size: 13px;
  font-weight: 400;
  color: ${({ theme }) => theme.colors.muted};
  opacity: 0.85;
`;

// Input · Textarea · Select 공통: 테두리 + 포커스되면 파란 테두리와 옅은 빛
const field = css`
  width: 100%;
  padding: 10px 12px;
  border: 1px solid ${({ theme }) => theme.colors.borderStrong};
  border-radius: ${({ theme }) => theme.radius};
  font: inherit;
  font-size: 15px;
  font-weight: 400;
  color: ${({ theme }) => theme.colors.text};
  background: ${({ theme }) => theme.colors.white};
  transition:
    border-color 0.15s,
    box-shadow 0.15s;

  &::placeholder {
    color: #9ca3af;
  }

  &:hover:not(:disabled) {
    border-color: #9ca3af;
  }

  &:focus,
  &:focus-visible {
    outline: none;
    border-color: ${({ theme }) => theme.colors.primary};
    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
  }

  &:disabled {
    color: ${({ theme }) => theme.colors.muted};
    background: ${({ theme }) => theme.colors.background};
    cursor: not-allowed;
  }
`;

export const Input = styled.input`
  ${field}

  /* 첨부 파일 선택 칸 */
  &[type='file'] {
    padding: 8px;
    font-size: 14px;
    color: ${({ theme }) => theme.colors.muted};
    cursor: pointer;
  }

  &[type='file']::file-selector-button {
    margin-right: 12px;
    padding: 6px 12px;
    border: 1px solid ${({ theme }) => theme.colors.border};
    border-radius: 6px;
    background: ${({ theme }) => theme.colors.background};
    font: inherit;
    font-size: 13px;
    font-weight: 600;
    color: ${({ theme }) => theme.colors.text};
    cursor: pointer;
  }

  &[type='file']:disabled::file-selector-button {
    cursor: not-allowed;
    opacity: 0.6;
  }
`;

export const Textarea = styled.textarea`
  ${field}
  min-height: 200px;
  line-height: 1.6;
  resize: none; /* 크기 고정, 내용이 넘치면 칸 안에서 스크롤 */
`;

export const Select = styled.select`
  ${field}
  width: auto;
  min-width: 160px;
  padding-right: 36px;
  appearance: none;
  /* 기본 화살표 대신 같은 모양의 화살표를 그린다 (브라우저마다 달라 보이지 않게) */
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%236b7280' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  cursor: pointer;
`;

export const Button = styled.button`
  display: inline-flex; /* 버튼 안의 스피너와 글자를 가운데 한 줄로 */
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border: none;
  border-radius: ${({ theme }) => theme.radius};
  background: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font-size: 15px;
  font-weight: 700;
  transition:
    background 0.15s,
    transform 0.05s;

  &:hover:not(:disabled) {
    background: ${({ theme }) => theme.colors.primaryHover};
  }

  &:active:not(:disabled) {
    transform: translateY(1px);
  }

  &:disabled {
    opacity: 0.6;
    cursor: default;
  }
`;

// 흰 바탕의 보조 버튼 (이전 · 다음 · 수정 · 삭제). as={Link} 로 링크로도 쓴다
export const SubButton = styled.button`
  display: inline-flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  min-height: 36px;
  padding: 0 14px;
  border: 1px solid ${({ theme }) => theme.colors.borderStrong};
  border-radius: ${({ theme }) => theme.radius};
  background: ${({ theme }) => theme.colors.white};
  font-size: 14px;
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
  color: ${({ theme, $danger }) => ($danger ? theme.colors.danger : theme.colors.text)};
  transition:
    color 0.15s,
    background 0.15s,
    border-color 0.15s;

  &:hover:not(:disabled) {
    ${({ theme, $danger }) =>
      $danger
        ? css`
            border-color: #fca5a5;
            background: ${theme.colors.dangerSoft};
          `
        : css`
            border-color: #9ca3af;
            background: ${theme.colors.background};
          `}
  }

  &:disabled {
    color: #9ca3af;
    background: ${({ theme }) => theme.colors.background};
    cursor: default;
  }
`;

export const Message = styled.p`
  padding: 10px 14px;
  border-radius: ${({ theme }) => theme.radius};
  font-size: 14px;
  color: ${({ $error, theme }) => ($error ? theme.colors.danger : theme.colors.primaryHover)};
  background: ${({ $error, theme }) => ($error ? theme.colors.dangerSoft : theme.colors.primarySoft)};
`;
