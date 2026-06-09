package com.raczkowski.app.common.offset;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.util.function.Function;

@NoArgsConstructor
public final class OffsetPagination {

    public static int normalizeLimit(Integer limit, int def, int max) {
        int l = (limit == null) ? def : limit;
        if (l <= 0) l = def;
        if (l > max) l = max;
        return l;
    }

    public static int normalizeOffset(Integer offset) {
        return (offset == null || offset < 0) ? 0 : offset;
    }

    public static int pageFromOffset(int offset, int limit) {
        return offset / limit;
    }

    public static Pageable pageable(int offset, int limit, Sort sort) {
        return PageRequest.of(pageFromOffset(offset, limit), limit, sort == null ? Sort.unsorted() : sort);
    }

    public static <T> SliceResponse<T> toResponse(Slice<T> slice, int currentOffset) {
        boolean hasNext = slice.hasNext();
        int nextOffset = hasNext ? currentOffset + slice.getNumberOfElements() : -1;
        return new SliceResponse<>(slice.getContent(), hasNext, nextOffset);
    }

    public static <T> SliceResponse<T> fetch(
            Integer offset,
            Integer limit,
            Sort sort,
            int defaultLimit,
            int maxLimit,
            Function<Pageable, Slice<T>> fetcher
    ) {
        int o = normalizeOffset(offset);
        int l = normalizeLimit(limit, defaultLimit, maxLimit);
        Pageable pageable = pageable(o, l, sort);
        Slice<T> slice = fetcher.apply(pageable);
        return toResponse(slice, o);
    }
}
