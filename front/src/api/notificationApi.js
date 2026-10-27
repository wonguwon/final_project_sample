import client from './client';

// 알림 (REST). 실시간 알림은 notification/subscribeNotifications.js (SSE)
export const getNotifications = (size = 20) => client.get('/notifications', { params: { size } });
export const getUnreadCount = () => client.get('/notifications/unread-count');
export const readNotification = (id) => client.patch(`/notifications/${id}/read`);
export const readAllNotifications = () => client.patch('/notifications/read-all');
export const sendTestNotification = () => client.post('/notifications/test');
