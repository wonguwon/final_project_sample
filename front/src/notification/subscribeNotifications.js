import { fetchEventSource } from '@microsoft/fetch-event-source';
import { refreshAccessToken } from '../api/tokenRefresh';
import useAuthStore from '../stores/useAuthStore';

class RenewToken extends Error {} // 토큰이 만료됨 → 재발급 후 새로 연결
class StopRetry extends Error {} // 다시 연결해도 소용없음 → 멈춤

// 브라우저의 EventSource 는 Authorization 헤더를 붙일 수 없어서 fetch 로 SSE 를 받는다.
// 연결을 시작하고, 끊는 함수를 돌려준다
export const subscribeNotifications = (onNotification) => {
  const controller = new AbortController();

  const start = () => {
    const { accessToken } = useAuthStore.getState(); // 연결할 때마다 "지금" 토큰

    fetchEventSource('/api/v1/notifications/subscribe', {
      headers: { Authorization: `Bearer ${accessToken}` },
      signal: controller.signal,
      openWhenHidden: true, // 다른 탭을 보고 있어도 연결을 유지

      async onopen(res) {
        if (res.ok) return; // 200 → 연결 성공
        const code = (await res.json().catch(() => null))?.error?.code;
        throw code === 'TOKEN_EXPIRED' ? new RenewToken() : new StopRetry();
      },

      onmessage(ev) {
        if (ev.event === 'notification') onNotification(JSON.parse(ev.data));
        // connected 이벤트와 :ping 주석은 무시
      },

      onclose() {
        throw new Error('서버가 연결을 닫음'); // 30분 시간 초과 등 → onerror 로 가서 다시 연결
      },

      onerror(err) {
        if (err instanceof RenewToken || err instanceof StopRetry) throw err; // 재시도 멈춤
        return 3000; // 네트워크 문제 · 서버 재시작 → 3초 뒤 다시 연결
      },
    }).catch((err) => {
      if (err instanceof RenewToken) {
        refreshAccessToken()
          .then(start)
          .catch(() => {}); // 새 토큰으로 처음부터. 재발급 실패 = 로그아웃
      }
    });
  };

  start();
  return () => controller.abort(); // 로그아웃 · 화면 종료 때 연결 끊기
};
