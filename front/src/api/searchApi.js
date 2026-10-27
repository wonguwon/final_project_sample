import client from './client';

// 뉴스 검색 — React 는 Elasticsearch(9200)를 직접 부르지 않는다. 항상 Spring 을 거친다
export const searchNews = ({ q, sentiment, sort, page }) =>
  client.get('/search/news', { params: { q, sentiment, sort, page, size: 10 } });
