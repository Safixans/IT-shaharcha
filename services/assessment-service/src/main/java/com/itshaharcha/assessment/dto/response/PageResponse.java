package com.itshaharcha.assessment.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Generic pagination envelope (spec Page). */
public record PageResponse<T>(
        List<T> items,
        PageMeta meta) {

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        List<T> items = page.getContent().stream().map(mapper).toList();
        PageMeta meta = new PageMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
        return new PageResponse<>(items, meta);
    }
}
