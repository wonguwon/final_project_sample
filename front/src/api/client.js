import axios from 'axios';
import useAuthStore from '../stores/useAuthStore';
import { refreshAccessToken } from './tokenRefresh';

// 모든 API 호출이 이 인스턴스를 거친다.
// baseURL 이 상대 경로('/api/v1')라서 요청은 Vite 개발 서버(5173)로 가고,
// vite.config.js 의 proxy 가 실제 백엔드로 전달한다.
const client = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
});

// 요청 인터셉터: 로그인 상태라면 모든 요청에 access token 을 붙인다
client.interceptors.request.use((config) => {
  const { accessToken } = useAuthStore.getState();
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

// 응답 인터셉터: access token 이 만료되었으면 새로 받아서 원래 요청을 한 번 더 보낸다
client.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    const code = error.response?.data?.error?.code;

    // _retry: 다시 보낸 요청이 또 실패해도 무한 반복하지 않도록 표시
    if (code === 'TOKEN_EXPIRED' && original && !original._retry) {
      original._retry = true;
      const accessToken = await refreshAccessToken(); // 실패하면 예외가 그대로 호출한 쪽으로 간다
      original.headers.Authorization = `Bearer ${accessToken}`;
      return client(original);
    }
    return Promise.reject(error);
  }
);

export default client;
