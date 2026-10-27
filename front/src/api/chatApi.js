import client from './client';

// 채팅방 · 지난 메시지 (REST). 실시간 메시지는 chat/stompClient.js (WebSocket)
export const getRooms = () => client.get('/chat/rooms');
export const createRoom = (title) => client.post('/chat/rooms', { title });
export const joinRoom = (roomId) => client.post(`/chat/rooms/${roomId}/members`);
export const getMessages = (roomId, beforeId) => client.get(`/chat/rooms/${roomId}/messages`, { params: { beforeId } });
