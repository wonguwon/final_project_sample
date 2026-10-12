import { create } from 'zustand';

// 로그인 상태.
// access token 은 이 스토어(메모리)에만 둔다. localStorage 에 저장하지 않는다.
// 새로고침하면 사라지지만, 앱이 시작될 때 refresh token 쿠키로 다시 받아 온다 (App.jsx).
const useAuthStore = create((set) => ({
  accessToken: null,
  member: null, // { memberId, loginId, nickname }
  initialized: false, // 앱 시작 시 로그인 복원 시도가 끝났는지

  setAuth: ({ accessToken, member }) => set({ accessToken, member }),
  clearAuth: () => set({ accessToken: null, member: null }),
  setInitialized: () => set({ initialized: true }),
}));

export default useAuthStore;
