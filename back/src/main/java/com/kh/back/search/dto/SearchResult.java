package com.kh.back.search.dto;

import java.util.List;

public record SearchResult(List<NewsSearchItem> items, int total) {}
