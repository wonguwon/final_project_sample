package com.kh.back.search.controller;

import com.kh.back.common.dto.ApiResponse;
import com.kh.back.common.dto.PageMeta;
import com.kh.back.search.dto.NewsSearchItem;
import com.kh.back.search.dto.SearchResult;
import com.kh.back.search.service.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchApiController {

    private final SearchService searchService;

    // GET /api/v1/search/news?q=금리&sentiment=positive&sort=latest&page=1&size=10
    @GetMapping("/news")
    public ApiResponse<List<NewsSearchItem>> news(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sentiment,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 50);   // 한 번에 최대 50건

        SearchResult result = searchService.searchNews(q, sentiment, sort, page, size);
        return ApiResponse.ok(result.items(), PageMeta.of(page, size, result.total()));
    }
}
