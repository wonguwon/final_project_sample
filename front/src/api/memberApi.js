import client from './client';

// 내 정보 — access token 이 있어야 한다 (client 가 자동으로 붙인다)
export const getMe = () => client.get('/members/me');
