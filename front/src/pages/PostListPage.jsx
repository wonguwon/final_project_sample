import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import styled from 'styled-components';
import { getCompanies } from '../api/companyApi';
import { getPosts } from '../api/postApi';
import { Message, Select, SubButton } from '../components/FormElements';
import { PageSpinner } from '../components/Spinner';
import useAuthStore from '../stores/useAuthStore';
import { errorMessage, formatDateTime } from '../utils/format';

const Toolbar = styled.div`
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
`;

const WriteLink = styled(Link)`
  margin-left: auto;
  padding: 9px 18px;
  border-radius: ${({ theme }) => theme.radius};
  background: ${({ theme }) => theme.colors.primary};
  color: ${({ theme }) => theme.colors.white};
  font-size: 15px;
  font-weight: 700;
  white-space: nowrap;
  transition: background 0.15s;

  &:hover {
    background: ${({ theme }) => theme.colors.primaryHover};
  }
`;

// 화면이 좁으면 표만 가로로 스크롤 (페이지 전체가 밀리지 않게)
const TableWrap = styled.div`
  overflow-x: auto;
  background: ${({ theme }) => theme.colors.white};
  border: 1px solid ${({ theme }) => theme.colors.border};
  border-radius: ${({ theme }) => theme.radiusLg};
  box-shadow: ${({ theme }) => theme.shadow};
`;

const Table = styled.table`
  width: 100%;
  min-width: 560px;
  border-collapse: collapse;

  th,
  td {
    padding: 14px 16px;
    border-bottom: 1px solid ${({ theme }) => theme.colors.border};
    text-align: left;
    white-space: nowrap;
  }

  th {
    padding-top: 12px;
    padding-bottom: 12px;
    font-size: 13px;
    font-weight: 600;
    color: ${({ theme }) => theme.colors.muted};
    background: ${({ theme }) => theme.colors.background};
  }

  td {
    font-size: 15px;
  }

  tbody tr:last-child td {
    border-bottom: none;
  }

  tbody tr:hover td {
    background: #f8fafc;
  }

  /* 제목 칸만 남는 폭을 모두 쓰고, 너무 길면 … 로 줄인다 */
  .title {
    width: 100%;
    max-width: 0;
    white-space: normal;
  }

  .title a {
    font-weight: 500;
  }

  .title a:hover {
    color: ${({ theme }) => theme.colors.primary};
  }

  .company {
    color: ${({ theme }) => theme.colors.muted};
    font-size: 14px;
  }

  /* 휴대폰 폭에서는 종목 · 작성일 칸을 숨겨 제목이 잘 보이게 */
  @media (max-width: 600px) {
    min-width: 0;

    th,
    td {
      padding-left: 12px;
      padding-right: 12px;
    }

    .company,
    .date {
      display: none;
    }
  }
`;

const TitleLine = styled.div`
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;

  a {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
`;

const Muted = styled.span`
  font-size: 13px;
  color: ${({ theme }) => theme.colors.muted};
`;

const CommentCount = styled.span`
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 700;
  color: ${({ theme }) => theme.colors.primary};
`;

const Empty = styled.td`
  /* && : Table 의 td 규칙보다 우선하도록 */
  && {
    padding: 56px 16px;
    text-align: center;
    color: ${({ theme }) => theme.colors.muted};
  }
`;

const Pager = styled.div`
  display: flex;
  gap: 12px;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  font-size: 14px;
  color: ${({ theme }) => theme.colors.muted};

  strong {
    color: ${({ theme }) => theme.colors.text};
  }
`;

const ErrorBox = styled(Message)`
  margin-bottom: 16px;
`;

function PostListPage() {
  // 종목 · 페이지는 주소(?code=G0001&page=2)에 둔다 → 새로고침 · 뒤로 가기 · 주소 공유에도 그대로 유지
  const [searchParams, setSearchParams] = useSearchParams();
  const code = searchParams.get('code') ?? '';
  const page = Number(searchParams.get('page') ?? 1);

  const [companies, setCompanies] = useState([]);
  const [posts, setPosts] = useState([]);
  const [meta, setMeta] = useState(null);
  const [error, setError] = useState(null);
  const [loadedKey, setLoadedKey] = useState(null); // 마지막으로 응답을 받은 "종목|페이지"
  const member = useAuthStore((s) => s.member);

  // 지금 주소의 응답이 아직 안 왔으면 로딩 중이다.
  // (effect 안에서 setLoading(true) 를 부르면 렌더링이 한 번 더 일어나므로, 상태 대신 계산으로 구한다)
  const loading = loadedKey !== `${code}|${page}`;

  // 종목 선택 목록 (한 번만)
  useEffect(() => {
    getCompanies(1, 100).then((res) => setCompanies(res.data.data));
  }, []);

  // 주소의 code · page 가 바뀔 때마다 다시 불러온다
  useEffect(() => {
    getPosts({ code, page })
      .then((res) => {
        setPosts(res.data.data);
        setMeta(res.data.meta);
        setError(null);
      })
      .catch((err) => setError(errorMessage(err)))
      .finally(() => setLoadedKey(`${code}|${page}`));
  }, [code, page]);

  const changeCode = (e) => {
    const next = e.target.value;
    setSearchParams(next ? { code: next } : {}); // 종목을 바꾸면 1페이지부터
  };

  const goPage = (next) => {
    setSearchParams(code ? { code, page: next } : { page: next });
  };

  return (
    <section>
      <h2>종목 토론</h2>
      <Toolbar>
        <Select value={code} onChange={changeCode}>
          <option value="">전체 종목</option>
          {companies.map((c) => (
            <option key={c.code} value={c.code}>
              {c.name}
            </option>
          ))}
        </Select>
        {/* 로그인한 사람에게만 글쓰기 버튼. 선택한 종목을 작성 화면에 넘긴다 */}
        {member && <WriteLink to={code ? `/posts/new?code=${code}` : '/posts/new'}>글쓰기</WriteLink>}
      </Toolbar>

      {error && <ErrorBox $error>불러오지 못했습니다: {error}</ErrorBox>}

      {loading ? (
        <PageSpinner />
      ) : (
        <TableWrap>
          <Table>
            <thead>
              <tr>
                <th className="company">종목</th>
                <th>제목</th>
                <th>작성자</th>
                <th className="date">작성일</th>
              </tr>
            </thead>
            <tbody>
              {posts.map((p) => (
                <tr key={p.postId}>
                  <td className="company">{p.companyName}</td>
                  <td className="title">
                    <TitleLine>
                      <Link to={`/posts/${p.postId}`}>{p.title}</Link>
                      {p.commentCount > 0 && <CommentCount>[{p.commentCount}]</CommentCount>}
                      {p.fileCount > 0 && <Muted>📎</Muted>}
                    </TitleLine>
                  </td>
                  <td>{p.nickname}</td>
                  <td className="date">
                    <Muted>{formatDateTime(p.createdAt)}</Muted>
                  </td>
                </tr>
              ))}
              {posts.length === 0 && (
                <tr>
                  <Empty colSpan={4}>아직 글이 없습니다.</Empty>
                </tr>
              )}
            </tbody>
          </Table>
        </TableWrap>
      )}

      {meta && meta.totalPages > 1 && (
        <Pager>
          <SubButton disabled={page <= 1} onClick={() => goPage(page - 1)}>
            이전
          </SubButton>
          <span>
            <strong>{page}</strong> / {meta.totalPages}
          </span>
          <SubButton disabled={page >= meta.totalPages} onClick={() => goPage(page + 1)}>
            다음
          </SubButton>
        </Pager>
      )}
    </section>
  );
}

export default PostListPage;
