package uz.thompson.appmockielts.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationDto<T> {

    private int totalPages;

    private int page;

    private int size;

    private long totalElements;

    private T content;

    private boolean hasPrevious;

    private boolean hasNext;

    private PaginationDto(int page, int size, T content, boolean hasPrevious, boolean hasNext) {
        this.page = page;
        this.size = size;
        this.content = content;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
    }

    private PaginationDto(int pageCount, int page, int size, long count, T content) {
        this.totalPages = pageCount;
        this.page = page;
        this.size = size;
        this.totalElements = count;
        this.content = content;
    }

    public static <E> PaginationDto<E> makeForSlice(int page, int size, E content, boolean hasPrevious, boolean hasNext) {
        return new PaginationDto<>(page, size, content, hasPrevious, hasNext);
    }

    public static <E> PaginationDto<E> makeForPage(int totalPages, int page, int size, long totalElements, E content) {
        return new PaginationDto<>(totalPages, page, size, totalElements, content);
    }
}