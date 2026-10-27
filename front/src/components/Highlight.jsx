// 서버가 강조 위치를 (시작) · (끝) 문자로 표시해 보낸다.
// "가온전자, 금리 인하"  →  가온전자, <mark>금리</mark> 인하
// HTML 문자열을 끼워 넣지 않고(dangerouslySetInnerHTML 금지) React 요소로 만든다 → 본문에 섞인 코드가 실행되지 않는다
function Highlight({ text }) {
  if (!text) return null;
  const parts = text.split(/|/); // 표시 문자로 자르면 [밖, 안, 밖, 안, 밖 ...] 순서가 된다
  return parts.map((part, i) => (i % 2 === 1 ? <mark key={i}>{part}</mark> : <span key={i}>{part}</span>));
}

export default Highlight;
