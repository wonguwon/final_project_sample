import react, { reactCompilerPreset } from '@vitejs/plugin-react';
import babel from '@rolldown/plugin-babel';
import { defineConfig } from 'vite';

// /api 로 시작하는 요청을 Spring Boot 로 전달한다 (개발 서버 전용)
const API_TARGET = 'http://localhost:8080';

export default defineConfig({
  plugins: [react(), babel({ presets: [reactCompilerPreset()] })],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: API_TARGET,
        changeOrigin: true,
      },
      // 채팅 WebSocket. ws: true 가 있어야 WebSocket 연결을 넘겨준다
      '/ws': {
        target: API_TARGET,
        ws: true,
      },
    },
  },
});
