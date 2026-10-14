const theme = {
  colors: {
    primary: '#2563eb',
    primaryHover: '#1d4ed8',
    primarySoft: '#eff6ff', // 선택된 메뉴 · 입력칸 포커스 테두리 빛
    up: '#dc2626', // 상승 (국내 주식 화면 관례: 빨강)
    down: '#2563eb', // 하락 (파랑)
    danger: '#dc2626', // 삭제 · 오류
    dangerSoft: '#fef2f2',
    text: '#1f2937',
    muted: '#6b7280',
    border: '#e5e7eb',
    borderStrong: '#d1d5db', // 입력칸 테두리 (카드 테두리보다 조금 진하게)
    background: '#f9fafb',
    white: '#ffffff',
  },
  radius: '8px',
  radiusLg: '12px',
  shadow: '0 1px 2px rgba(15, 23, 42, 0.04), 0 1px 3px rgba(15, 23, 42, 0.06)',
  maxWidth: '1080px',
  gutter: '24px', // 본문 좌우 여백 (calc() 안에서 쓰므로 px 단위 문자열)
};

export default theme;
