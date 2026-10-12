import styled from 'styled-components';

// 로그인 · 회원가입 화면이 함께 쓰는 폼 스타일

export const Form = styled.form`
  max-width: 360px;
  margin: 40px auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

export const Label = styled.label`
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
  color: ${({ theme }) => theme.colors.muted};
`;

export const Input = styled.input`
  padding: 10px 12px;
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radius};
  font-size: 15px;
  color: ${({ theme }) => theme.colors.text};
  background: ${({ theme }) => theme.colors.white};
`;

export const Button = styled.button`
  padding: 12px;
  border: none;
  border-radius: ${({ theme }) => theme.radius};
  background: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font-weight: 700;

  &:disabled {
    opacity: 0.6;
    cursor: default;
  }
`;

export const Message = styled.p`
  font-size: 14px;
  color: ${({ $error, theme }) => ($error ? theme.colors.up : theme.colors.primary)};
`;
