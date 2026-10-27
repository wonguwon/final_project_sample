package com.kh.back.search.service;

import com.kh.back.search.dto.SearchResult;

public interface SearchService {

    SearchResult searchNews(String q, String sentiment, String sort, int page, int size);
}
