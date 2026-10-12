import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { refreshAccessToken } from './api/tokenRefresh';
import Layout from './components/Layout';
import RequireAuth from './components/RequireAuth';
import CompanyDetailPage from './pages/CompanyDetailPage';
import CompanyListPage from './pages/CompanyListPage';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import MyPage from './pages/MyPage';
import NotFound from './pages/NotFound';
import PredictPage from './pages/PredictPage';
import SearchPage from './pages/SearchPage';
import SignupPage from './pages/SignupPage';
import useAuthStore from './stores/useAuthStore';

function App() {
  // 앱이 처음 열릴 때 한 번: refresh token 쿠키가 있으면 로그인 상태를 되살린다
  useEffect(() => {
    refreshAccessToken()
      .catch(() => {}) // 쿠키가 없거나 만료 → 로그아웃 상태로 시작 (정상)
      .finally(() => useAuthStore.getState().setInitialized());
  }, []);

  return (
    <Routes>
      {/* Layout 안의 <Outlet /> 자리에 아래 페이지들이 바뀌어 들어간다 */}
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/companies" element={<CompanyListPage />} />
        <Route path="/companies/:code" element={<CompanyDetailPage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/predict" element={<PredictPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />

        {/* 로그인한 사람만 들어갈 수 있는 화면 */}
        <Route element={<RequireAuth />}>
          <Route path="/mypage" element={<MyPage />} />
        </Route>
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}

export default App;
