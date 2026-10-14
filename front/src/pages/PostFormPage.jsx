import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import styled from 'styled-components';
import { getCompanies } from '../api/companyApi';
import { createPost, getPost, updatePost } from '../api/postApi';
import { Button, Hint, Input, Label, Message, Select, Textarea } from '../components/FormElements';
import { ButtonSpinner, PageSpinner } from '../components/Spinner';
import useAuthStore from '../stores/useAuthStore';
import { errorMessage, formatFileSize } from '../utils/format';

const MAX_FILES = 5;
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 서버(application.yaml)와 같은 값으로 먼저 걸러 준다

const Form = styled.form`
  max-width: 720px;
  margin: 0 auto;
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: ${({ theme }) => theme.colors.white};
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radiusLg};
  box-shadow: ${({ theme }) => theme.shadow};

  h2 {
    margin-bottom: 0;
  }

  textarea {
    min-height: 280px;
  }

  /* 등록 버튼은 오른쪽 아래에 */
  button[type='submit'] {
    align-self: flex-end;
    min-width: 140px;
    padding: 12px 24px;
  }

  @media (max-width: 600px) {
    padding: 20px 16px;

    button[type='submit'] {
      align-self: stretch;
    }
  }
`;

const FileList = styled.ul`
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: -8px; /* 위의 파일 선택 칸에 붙여 보이게 */

  &:empty {
    display: none;
  }
`;

const FileRow = styled.li`
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radius};
  background: ${({ theme }) => theme.colors.background};
  font-size: 14px;

  span {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  button {
    flex-shrink: 0;
    padding: 2px 8px;
    border: none;
    border-radius: 6px;
    background: none;
    font-size: 13px;
    font-weight: 600;
    color: ${({ theme }) => theme.colors.danger};
  }

  button:hover {
    background: ${({ theme }) => theme.colors.dangerSoft};
  }
`;

const Removed = styled.span`
  text-decoration: line-through;
  color: ${({ theme }) => theme.colors.muted};
`;

// /posts/new (작성) 과 /posts/:postId/edit (수정) 을 함께 처리한다
function PostFormPage() {
  const { postId } = useParams();
  const isEdit = Boolean(postId);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const member = useAuthStore((s) => s.member);

  const [companies, setCompanies] = useState([]);
  const [form, setForm] = useState({ companyCode: searchParams.get('code') ?? '', title: '', content: '' });
  const [existingFiles, setExistingFiles] = useState([]); // 수정할 때 이미 붙어 있는 파일
  const [deleteFileIds, setDeleteFileIds] = useState([]); // 그중 지우기로 표시한 파일 번호
  const [newFiles, setNewFiles] = useState([]); // 새로 고른 파일 (File 객체)
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(isEdit); // 수정이면 기존 글을 불러오는 동안 로딩 표시

  useEffect(() => {
    getCompanies(1, 100).then((res) => setCompanies(res.data.data));
  }, []);

  // 수정이면 기존 글을 불러와 채운다
  useEffect(() => {
    if (!isEdit) return;
    getPost(postId)
      .then((res) => {
        const post = res.data.data;
        if (post.memberId !== member?.memberId) {
          setError('작성자만 수정할 수 있습니다.');
          return;
        }
        setForm({ companyCode: post.companyCode, title: post.title, content: post.content });
        setExistingFiles(post.files);
      })
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, [isEdit, postId, member]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const remainingCount = existingFiles.length - deleteFileIds.length + newFiles.length;

  // 파일 선택 — 고른 파일을 목록에 더한다. 개수 · 크기는 서버도 검사하지만 먼저 알려 주면 편하다
  const handleFiles = (e) => {
    const picked = Array.from(e.target.files);
    e.target.value = ''; // 같은 파일을 다시 고를 수 있게 비운다

    const tooBig = picked.find((f) => f.size > MAX_FILE_SIZE);
    if (tooBig) {
      setError(`${tooBig.name}: 파일 하나는 10MB 까지 올릴 수 있습니다.`);
      return;
    }
    if (remainingCount + picked.length > MAX_FILES) {
      setError(`첨부 파일은 ${MAX_FILES}개까지 올릴 수 있습니다.`);
      return;
    }
    setError(null);
    setNewFiles([...newFiles, ...picked]);
  };

  const toggleDelete = (fileId) =>
    setDeleteFileIds(
      deleteFileIds.includes(fileId) ? deleteFileIds.filter((id) => id !== fileId) : [...deleteFileIds, fileId]
    );

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);

    // 파일이 함께 가므로 JSON 이 아니라 FormData 로 만든다.
    // 같은 이름(files, deleteFileIds)으로 여러 번 append 하면 서버에서 List 로 받는다
    const data = new FormData();
    data.append('title', form.title);
    data.append('content', form.content);
    newFiles.forEach((file) => data.append('files', file));

    try {
      let res;
      if (isEdit) {
        deleteFileIds.forEach((id) => data.append('deleteFileIds', id));
        res = await updatePost(postId, data);
      } else {
        data.append('companyCode', form.companyCode);
        res = await createPost(data);
      }
      navigate(`/posts/${res.data.data.postId}`, { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <PageSpinner />;

  return (
    <Form onSubmit={handleSubmit}>
      <h2>{isEdit ? '글 수정' : '글쓰기'}</h2>

      <Label>
        종목
        <Select name="companyCode" value={form.companyCode} onChange={handleChange} disabled={isEdit}>
          <option value="">종목 선택</option>
          {companies.map((c) => (
            <option key={c.code} value={c.code}>
              {c.name}
            </option>
          ))}
        </Select>
      </Label>
      <Label>
        제목
        <Input name="title" value={form.title} onChange={handleChange} maxLength={100} />
      </Label>
      <Label>
        내용
        <Textarea name="content" value={form.content} onChange={handleChange} />
      </Label>

      <Label>
        <span>
          첨부 파일 ({remainingCount}/{MAX_FILES}) <Hint>— 이미지 · PDF · 문서 · zip, 파일당 10MB</Hint>
        </span>
        <Input type="file" multiple onChange={handleFiles} disabled={remainingCount >= MAX_FILES} />
      </Label>
      <FileList>
        {existingFiles.map((f) => (
          <FileRow key={f.fileId}>
            {deleteFileIds.includes(f.fileId) ? <Removed>{f.originalName}</Removed> : <span>{f.originalName}</span>}
            <button type="button" onClick={() => toggleDelete(f.fileId)}>
              {deleteFileIds.includes(f.fileId) ? '되돌리기' : '삭제'}
            </button>
          </FileRow>
        ))}
        {newFiles.map((f, i) => (
          <FileRow key={`${f.name}-${i}`}>
            <span>
              🆕 {f.name} ({formatFileSize(f.size)})
            </span>
            <button type="button" onClick={() => setNewFiles(newFiles.filter((_, j) => j !== i))}>
              빼기
            </button>
          </FileRow>
        ))}
      </FileList>

      {error && <Message $error>{error}</Message>}
      {/* 처리 중에는 버튼을 막는다 → 두 번 눌러 글이 두 개 생기는 것을 방지 */}
      <Button type="submit" disabled={submitting}>
        {submitting ? (
          <>
            <ButtonSpinner /> 저장 중 (파일이 크면 시간이 걸립니다)
          </>
        ) : isEdit ? (
          '수정하기'
        ) : (
          '등록하기'
        )}
      </Button>
    </Form>
  );
}

export default PostFormPage;
