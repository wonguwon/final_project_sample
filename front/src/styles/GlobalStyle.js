import { createGlobalStyle } from 'styled-components';

const GlobalStyle = createGlobalStyle`
  *, *::before, *::after {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  html {
    -webkit-text-size-adjust: 100%;
  }

  body {
    min-height: 100vh;
    font-family: 'Pretendard Variable', 'Pretendard', 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;
    color: ${({ theme }) => theme.colors.text};
    background: ${({ theme }) => theme.colors.background};
    line-height: 1.5;
    word-break: keep-all; /* 한글이 단어 중간에서 줄바꿈되지 않게 */
    overflow-wrap: anywhere; /* 긴 영문 · URL 은 칸을 넘지 않게 */
    -webkit-font-smoothing: antialiased;
  }

  h2 {
    margin-bottom: 20px;
    font-size: 22px;
    font-weight: 700;
    letter-spacing: -0.02em;
    line-height: 1.35;
  }

  h3 {
    font-size: 17px;
    font-weight: 700;
    letter-spacing: -0.01em;
  }

  a {
    color: inherit;
    text-decoration: none;
  }

  button {
    font: inherit;
    cursor: pointer;
  }

  input,
  textarea,
  select {
    font: inherit;
  }

  img {
    display: block;
    max-width: 100%;
  }

  ul, ol {
    list-style: none;
  }

  /* 키보드로 이동할 때만 보이는 포커스 테두리 */
  :focus-visible {
    outline: 2px solid ${({ theme }) => theme.colors.primary};
    outline-offset: 2px;
  }

  ::selection {
    background: ${({ theme }) => theme.colors.primarySoft};
    color: ${({ theme }) => theme.colors.primaryHover};
  }

  @media (max-width: 600px) {
    h2 {
      margin-bottom: 16px;
      font-size: 20px;
    }
  }
`;

export default GlobalStyle;
