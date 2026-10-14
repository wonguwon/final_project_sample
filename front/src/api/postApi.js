import client from './client';

// 목록 — code 가 없으면 전체 종목
export const getPosts = ({ code, page = 1, size = 10 }) =>
  client.get('/posts', { params: { code: code || undefined, page, size } });

export const getPost = (postId) => client.get(`/posts/${postId}`);

// 작성 · 수정은 파일이 함께 가므로 JSON 이 아니라 FormData 로 보낸다.
// Content-Type 을 직접 정하지 않는다 → axios 가 boundary 가 붙은 multipart/form-data 로 정해 준다
export const createPost = (formData) => client.post('/posts', formData);

export const updatePost = (postId, formData) => client.put(`/posts/${postId}`, formData);

export const deletePost = (postId) => client.delete(`/posts/${postId}`);

// 댓글
export const getComments = (postId) => client.get(`/posts/${postId}/comments`);

export const addComment = (postId, content) => client.post(`/posts/${postId}/comments`, { content });

export const deleteComment = (postId, commentId) => client.delete(`/posts/${postId}/comments/${commentId}`);
