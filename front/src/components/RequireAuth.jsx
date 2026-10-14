import { Navigate, Outlet, useLocation } from 'react-router-dom';
import useAuthStore from '../stores/useAuthStore';
import { PageSpinner } from './Spinner';

// 로그인한 사람만 들어갈 수 있는 화면을 감싸는 레이아웃 라우트.
// 로그인 상태가 아니면 /login 으로 보내고, 로그인 후 원래 가려던 주소로 돌아올 수 있게 from 을 넘긴다.
function RequireAuth() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const initialized = useAuthStore((s) => s.initialized);
  const location = useLocation();

  // 새로고침 직후에는 refresh token 으로 로그인을 복원하는 중이다 → 결과가 나올 때까지 기다린다
  if (!initialized) return <PageSpinner />;

  if (!accessToken) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  return <Outlet />;
}

export default RequireAuth;
