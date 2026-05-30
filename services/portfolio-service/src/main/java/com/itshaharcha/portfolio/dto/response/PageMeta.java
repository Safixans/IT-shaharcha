package com.itshaharcha.portfolio.dto.response;

/** Pagination metadata (spec PageMeta). */
public record PageMeta(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {
}
