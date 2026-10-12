import useAuthStore from '../stores/useAuthStore';
import client from './client';

// 회원가입 — 성공해도 로그인 상태가 되지는 않는다
export const signup = (body) => client.post('/auth/signup', body);

// 로그인 — 응답의 access token · 회원 정보를 스토어에 넣는다 (refresh token 은 쿠키로 이미 저장됨)
export const login = async (body) => {
  const res = await client.post('/auth/login', body);
  useAuthStore.getState().setAuth(res.data.data);
  return res.data.data;
};

// 로그아웃 — 서버 요청이 실패해도 화면은 로그아웃 상태로 만든다
export const logout = async () => {
  try {
    await client.post('/auth/logout');
  } finally {
    useAuthStore.getState().clearAuth();
  }
};
