import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import styled from 'styled-components';
import { deletePost, getPost } from '../api/postApi';
import CommentSection from '../components/CommentSection';
import { Card, Message, SubButton } from '../components/FormElements';
import { PageSpinner } from '../components/Spinner';
import useAuthStore from '../stores/useAuthStore';
import { errorMessage, formatDateTime, formatFileSize } from '../utils/format';

const Article = styled(Card).attrs({ as: 'article' })`
  padding: 32px;

  @media (max-width: 600px) {
    padding: 20px 16px;
  }
`;

const Header = styled.div`
  padding-bottom: 20px;
  border-bottom: 1px solid ${({ theme }) => theme.colors.border};

  h2 {
    margin: 8px 0 12px;
    font-size: 26px;
    line-height: 1.35;
  }
`;

const BackLink = styled(Link)`
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: ${({ theme }) => theme.colors.primary};

  &::before {
    content: '‹';
    font-size: 18px;
    line-height: 1;
  }

  &:hover {
    color: ${({ theme }) => theme.colors.primaryHover};
  }
`;

const Meta = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  font-size: 14px;
  color: ${({ theme }) => theme.colors.muted};

  span:first-child {
    font-weight: 600;
    color: ${({ theme }) => theme.colors.text};
  }
`;

const Content = styled.p`
  margin: 28px 0;
  font-size: 16px;
  line-height: 1.8;
  white-space: pre-wrap; /* 줄바꿈을 그대로 보여 준다 */
`;

const Images = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  a {
    display: block;
    overflow: hidden;
    border: 1px solid ${({ theme }) => theme.colors.border};
    border-radius: ${({ theme }) => theme.radius};
    transition: box-shadow 0.15s;
  }

  a:hover {
    box-shadow: 0 4px 12px rgba(15, 23, 42, 0.12);
  }

  img {
    max-width: 320px;
    width: 100%;
  }
`;

const FileList = styled.ul`
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;

  li {
    padding: 10px 12px;
    border: 1px solid ${({ theme }) => theme.colors.border};
    border-radius: ${({ theme }) => theme.radius};
    background: ${({ theme }) => theme.colors.background};
    color: ${({ theme }) => theme.colors.muted};
  }

  a {
    font-weight: 600;
    color: ${({ theme }) => theme.colors.text};
  }

  a:hover {
    color: ${({ theme }) => theme.colors.primary};
    text-decoration: underline;
  }
`;

const Actions = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 28px;
`;

function PostDetailPage() {
  const { postId } = useParams();
  const [post, setPost] = useState(null);
  const [error, setError] = useState(null);
  const member = useAuthStore((s) => s.member);
  const navigate = useNavigate();

  useEffect(() => {
    getPost(postId)
      .then((res) => setPost(res.data.data))
      .catch((err) => setError(errorMessage(err)));
  }, [postId]);

  const handleDelete = async () => {
    if (!window.confirm('글을 삭제할까요? 첨부 파일과 댓글도 함께 삭제됩니다.')) return;
    try {
      await deletePost(postId);
      navigate(`/posts?code=${post.companyCode}`, { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  if (error) return <Message $error>{error}</Message>;
  if (!post) return <PageSpinner />;

  // 수정 · 삭제 버튼은 작성자에게만 보인다. 버튼을 숨기는 것은 편의일 뿐, 실제 권한 확인은 서버가 한다
  const isOwner = member?.memberId === post.memberId;
  const images = post.files.filter((f) => f.image);
  const others = post.files.filter((f) => !f.image);

  return (
    <Article>
      <Header>
        <BackLink to={`/posts?code=${post.companyCode}`}>{post.companyName} 토론</BackLink>
        <h2>{post.title}</h2>
        <Meta>
          <span>{post.nickname}</span>
          <span>{formatDateTime(post.createdAt)}</span>
          {post.updatedAt !== post.createdAt && <span>(수정됨 {formatDateTime(post.updatedAt)})</span>}
        </Meta>
      </Header>

      <Content>{post.content}</Content>

      {/* 이미지는 화면에 바로 보여 준다. src 는 서버가 내려준 url 그대로 (/api/v1/posts/{id}/files/{fileId}) */}
      {images.length > 0 && (
        <Images>
          {images.map((f) => (
            <a key={f.fileId} href={f.url} target="_blank" rel="noreferrer">
              <img src={f.url} alt={f.originalName} />
            </a>
          ))}
        </Images>
      )}

      {/* 이미지가 아닌 파일은 링크를 누르면 내려받는다 (서버가 Content-Disposition: attachment 로 보낸다) */}
      {others.length > 0 && (
        <FileList>
          {others.map((f) => (
            <li key={f.fileId}>
              📎 <a href={f.url}>{f.originalName}</a> ({formatFileSize(f.fileSize)})
            </li>
          ))}
        </FileList>
      )}

      {isOwner && (
        <Actions>
          <SubButton as={Link} to={`/posts/${post.postId}/edit`}>
            수정
          </SubButton>
          <SubButton $danger onClick={handleDelete}>
            삭제
          </SubButton>
        </Actions>
      )}

      <CommentSection postId={post.postId} />
    </Article>
  );
}

export default PostDetailPage;
