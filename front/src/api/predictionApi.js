import client from './client';

// 다음 거래일 거래량 급증 확률 — React → Spring → FastAPI
// React 는 FastAPI 주소를 모른다. 항상 Spring 의 /api/v1 만 부른다
export const getPrediction = (code) => client.get(`/companies/${code}/prediction`);
