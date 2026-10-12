import { useEffect, useState } from 'react';
import { getMe } from '../api/memberApi';

// 로그인한 사람만 들어올 수 있다 (App.jsx 에서 RequireAuth 로 감쌌다)
function MyPage() {
  const [me, setMe] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    // access token 이 만료되어 있어도 client 가 알아서 재발급 후 다시 요청한다
    getMe()
      .then((res) => setMe(res.data.data))
      .catch((err) => setError(err.response?.data?.error?.message ?? err.message));
  }, []);

  if (error) return <p>불러오지 못했습니다: {error}</p>;
  if (!me) return <p>불러오는 중…</p>;

  return (
    <section>
      <h2>내 정보</h2>
      <ul>
        <li>회원 번호: {me.memberId}</li>
        <li>아이디: {me.loginId}</li>
        <li>닉네임: {me.nickname}</li>
      </ul>
    </section>
  );
}

export default MyPage;
