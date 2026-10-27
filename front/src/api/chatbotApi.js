import client from './client';

// 챗봇 — React 는 OpenAI 를 직접 부르지 않는다. 키는 Spring 서버에만 있다
export const askChatbot = (message) => client.post('/chatbot/messages', { message });
export const getChatbotHistory = () => client.get('/chatbot/messages');
export const clearChatbotHistory = () => client.delete('/chatbot/messages');
