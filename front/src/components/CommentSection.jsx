import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import styled from 'styled-components';
import { addComment, deleteComment, getComments } from '../api/postApi';
import useAuthStore from '../stores/useAuthStore';
import { errorMessage, formatDateTime } from '../utils/format';
import { Button, Message, Textarea } from './FormElements';
import { ButtonSpinner, PageSpinner } from './Spinner';

const Wrapper = styled.section`
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid ${({ theme }) => theme.colors.border};

  h3 {
    margin-bottom: 4px;
  }
`;

const Item = styled.li`
  padding: 14px 0;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};

  p {
    margin-top: 4px;
    font-size: 15px;
    line-height: 1.6;
    white-space: pre-wrap; /* 줄바꿈을 그대로 보여 준다 */
  }
`;

const Meta = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: ${({ theme }) => theme.colors.muted};

  strong {
    font-size: 14px;
    color: ${({ theme }) => theme.colors.text};
  }
`;

const SmallButton = styled.button`
  margin-left: auto;
  padding: 2px 8px;
  border: none;
  border-radius: 6px;
  background: none;
  font-size: 13px;
  color: ${({ theme }) => theme.colors.muted};
  transition:
    color 0.15s,
    background 0.15s;

  &:hover {
    color: ${({ theme }) => theme.colors.danger};
    background: ${({ theme }) => theme.colors.dangerSoft};
  }
`;

const CommentForm = styled.form`
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 16px;

  textarea {
    min-height: 88px;
  }

  button {
    align-self: flex-end;
    min-width: 96px;
    min-height: 40px; /* 스피너로 바뀌어도 버튼 크기가 그대로 */
    padding: 9px 18px;
  }
`;

const LoginNotice = styled.p`
  margin-top: 16px;
  padding: 16px;
  border-radius: ${({ theme }) => theme.radius};
  background: ${({ theme }) => theme.colors.background};
  text-align: center;
  font-size: 14px;
  color: ${({ theme }) => theme.colors.muted};

  a {
    font-weight: 700;
    color: ${({ theme }) => theme.colors.primary};
  }

  a:hover {
    text-decoration: underline;
  }
`;

const ErrorBox = styled(Message)`
  margin-top: 12px;
`;

// 게시글 상세 아래의 댓글 목록 · 작성 · 삭제
function CommentSection({ postId }) {
  const [comments, setComments] = useState([]);
  const [content, setContent] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const member = useAuthStore((s) => s.member);

  const load = () =>
    getComments(postId)
      .then((res) => setComments(res.data.data))
      .catch((err) => setError(errorMessage(err)));

  useEffect(() => {
    getComments(postId)
      .then((res) => setComments(res.data.data))
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, [postId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await addComment(postId, content);
      setContent('');
      await load(); // 목록을 다시 불러와 방금 쓴 댓글을 보여 준다
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (commentId) => {
    if (!window.confirm('댓글을 삭제할까요?')) return;
    try {
      await deleteComment(postId, commentId);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <Wrapper>
      <h3>댓글 {comments.length}</h3>
      {loading && <PageSpinner />}
      <ul>
        {comments.map((c) => (
          <Item key={c.commentId}>
            <Meta>
              <strong>{c.nickname}</strong>
              <span>{formatDateTime(c.createdAt)}</span>
              {/* 내가 쓴 댓글에만 삭제 버튼. 실제 권한 확인은 서버가 한다 (다른 사람이면 403) */}
              {member?.memberId === c.memberId && (
                <SmallButton onClick={() => handleDelete(c.commentId)}>삭제</SmallButton>
              )}
            </Meta>
            <p>{c.content}</p>
          </Item>
        ))}
      </ul>

      {error && <ErrorBox $error>{error}</ErrorBox>}

      {member ? (
        <CommentForm onSubmit={handleSubmit}>
          <Textarea value={content} onChange={(e) => setContent(e.target.value)} placeholder="댓글을 입력하세요" />
          <Button type="submit" disabled={submitting}>
            {submitting ? <ButtonSpinner /> : '댓글 등록'}
          </Button>
        </CommentForm>
      ) : (
        <LoginNotice>
          댓글을 쓰려면 <Link to="/login">로그인</Link>하세요.
        </LoginNotice>
      )}
    </Wrapper>
  );
}

export default CommentSection;
