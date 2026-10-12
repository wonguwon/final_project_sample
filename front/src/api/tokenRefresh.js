import axios from 'axios';
import useAuthStore from '../stores/useAuthStore';

// 진행 중인 재발급 요청. 여러 곳에서 동시에 불려도 요청은 한 번만 보낸다.
// refresh token 은 쓸 때마다 새것으로 바뀌므로(rotation), 두 번 보내면 두 번째는 이미 바뀐 토큰이라 실패한다.
let refreshing = null;

// refresh token 쿠키로 새 access token 을 받아 스토어에 넣는다
export const refreshAccessToken = () => {
  if (!refreshing) {
    // client 가 아니라 axios 를 직접 쓴다 → client 의 응답 인터셉터를 다시 타지 않게
    refreshing = axios
      .post('/api/v1/auth/refresh') // 쿠키는 브라우저가 자동으로 실어 보낸다
      .then((res) => {
        useAuthStore.getState().setAuth(res.data.data);
        return res.data.data.accessToken;
      })
      .catch((err) => {
        useAuthStore.getState().clearAuth(); // 재발급 실패 = 로그아웃 상태
        throw err;
      })
      .finally(() => {
        refreshing = null;
      });
  }
  return refreshing;
};
