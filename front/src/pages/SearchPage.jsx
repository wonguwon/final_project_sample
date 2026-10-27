import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import styled from 'styled-components';
import { searchNews } from '../api/searchApi';
import { Button, Card, Input, Message, SubButton } from '../components/FormElements';
import Highlight from '../components/Highlight';
import { PageSpinner } from '../components/Spinner';
import { errorMessage } from '../utils/format';

const SENTIMENTS = [
  { value: '', label: '전체' },
  { value: 'positive', label: '긍정' },
  { value: 'neutral', label: '중립' },
  { value: 'negative', label: '부정' },
];
const SENTIMENT_LABEL = { positive: '긍정', neutral: '중립', negative: '부정' };

const SearchForm = styled.form`
  display: flex;
  gap: 8px;
  margin: 16px 0;

  button {
    min-width: 80px;
    padding: 10px 18px;
  }
`;

const Filters = styled.div`
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;

  .sort {
    margin-left: auto;
  }
`;

// 선택된 필터 버튼은 파란 글자 · 테두리
const Chip = styled(SubButton)`
  ${({ $active, theme }) =>
    $active &&
    `
    color: ${theme.colors.primary};
    border-color: ${theme.colors.primary};
    background: ${theme.colors.primarySoft};
  `}
`;

const Total = styled.p`
  margin-bottom: 12px;
  font-size: 14px;
  color: ${({ theme }) => theme.colors.muted};
`;

const ResultList = styled(Card).attrs({ as: 'ul' })`
  padding: 0;

  li {
    padding: 18px 20px;
    border-bottom: 1px solid ${({ theme }) => theme.colors.border};
  }

  li:last-child {
    border-bottom: none;
  }

  h3 {
    font-size: 16px;
    line-height: 1.5;
  }

  p {
    margin-top: 6px;
    font-size: 14px;
    line-height: 1.6;
    color: ${({ theme }) => theme.colors.muted};
  }

  small {
    display: block;
    margin-top: 8px;
    font-size: 13px;
    color: ${({ theme }) => theme.colors.muted};
  }

  mark {
    padding: 0 2px;
    border-radius: 3px;
    background: #fef08a;
    color: inherit;
  }
`;

const Pager = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
  font-size: 14px;
`;

// /search?q=흐름&sentiment=positive&sort=latest&page=2 — 검색 조건은 주소에 둔다
function SearchPage() {
  // 주소에 두면 새로고침 · 뒤로가기 · 링크 공유가 그대로 된다
  const [searchParams, setSearchParams] = useSearchParams();
  const q = searchParams.get('q') ?? '';
  const sentiment = searchParams.get('sentiment') || undefined;
  const sort = searchParams.get('sort') || undefined;
  const page = Number(searchParams.get('page') ?? 1);

  const [input, setInput] = useState(q);
  const [result, setResult] = useState({ key: null, items: [], meta: null, error: null }); // 마지막으로 받은 응답

  const key = `${q}|${sentiment}|${sort}|${page}`; // 지금 주소의 검색 조건
  const loading = q !== '' && result.key !== key;

  useEffect(() => {
    if (!q) return;
    searchNews({ q, sentiment, sort, page })
      .then((res) => setResult({ key, items: res.data.data, meta: res.data.meta, error: null }))
      .catch((err) => setResult({ key, items: [], meta: null, error: errorMessage(err) }));
  }, [key, q, sentiment, sort, page]);

  // 검색은 Enter · 검색 버튼을 누를 때만 (글자를 칠 때마다 요청하지 않는다)
  const submit = (e) => {
    e.preventDefault();
    const keyword = input.trim();
    if (!keyword) return;
    setSearchParams({ q: keyword }); // 새 검색은 1페이지 · 필터 초기화
  };

  const changeParam = (name, value) => {
    const next = new URLSearchParams(searchParams);
    if (value) next.set(name, value);
    else next.delete(name);
    if (name !== 'page') next.delete('page'); // 필터 · 정렬을 바꾸면 1페이지로
    setSearchParams(next);
  };

  const shown = result.key === key ? result : null; // 지금 조건의 응답만 보여 준다

  return (
    <section>
      <h2>뉴스 검색</h2>

      <SearchForm onSubmit={submit}>
        <Input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="검색어 (예: 흐름, 가온전자)"
          maxLength={100}
        />
        <Button type="submit" disabled={!input.trim()}>
          검색
        </Button>
      </SearchForm>

      {q && (
        <Filters>
          {SENTIMENTS.map((s) => (
            <Chip
              key={s.value}
              $active={(sentiment ?? '') === s.value}
              onClick={() => changeParam('sentiment', s.value)}
            >
              {s.label}
            </Chip>
          ))}
          <span className="sort">
            <Chip $active={!sort} onClick={() => changeParam('sort', '')}>
              관련도순
            </Chip>{' '}
            <Chip $active={sort === 'latest'} onClick={() => changeParam('sort', 'latest')}>
              최신순
            </Chip>
          </span>
        </Filters>
      )}

      {loading && <PageSpinner />}
      {shown?.error && <Message $error>{shown.error}</Message>}

      {shown && !shown.error && shown.items.length === 0 && <p>'{q}'에 대한 결과가 없습니다.</p>}

      {shown && shown.items.length > 0 && (
        <>
          <Total>{shown.meta.total.toLocaleString()}건</Total>
          <ResultList>
            {shown.items.map((item) => (
              <li key={item.newsId}>
                <h3>
                  <Highlight text={item.title} />
                </h3>
                {item.snippet && (
                  <p>
                    …<Highlight text={item.snippet} />…
                  </p>
                )}
                <small>
                  {item.companyNames?.join(', ')} · {SENTIMENT_LABEL[item.sentiment] ?? item.sentiment} ·{' '}
                  {item.publishedAt?.slice(0, 10)}
                </small>
              </li>
            ))}
          </ResultList>

          <Pager>
            <SubButton disabled={page <= 1} onClick={() => changeParam('page', String(page - 1))}>
              이전
            </SubButton>
            {page} / {shown.meta.totalPages}
            <SubButton disabled={page >= shown.meta.totalPages} onClick={() => changeParam('page', String(page + 1))}>
              다음
            </SubButton>
          </Pager>
        </>
      )}
    </section>
  );
}

export default SearchPage;
