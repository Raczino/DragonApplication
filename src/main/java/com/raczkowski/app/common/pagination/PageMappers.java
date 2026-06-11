package com.raczkowski.app.common.pagination;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@NoArgsConstructor
public final class PageMappers {
    public static <T, R> PageResponse<R> paginateAndMap(
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Function<Pageable, Page<T>> pageSupplier,
            Function<T, R> mapper
    ) {
        Page<T> page = GenericService.paginate(pageNumber, pageSize, sortBy, sortDirection, pageSupplier);
        List<R> content = page.stream().map(mapper).toList();
        return toResponse(page, content);
    }

    public static <T, R> PageResponse<R> mapPageAndEnrich(
            Page<T> page,
            Function<T, R> mapper,
            BiConsumer<List<R>, List<T>> enricher
    ) {
        List<T> entities = page.getContent();
        List<R> items = new ArrayList<>(entities.size());
        for (T e : entities) items.add(mapper.apply(e));
        if (enricher != null && !entities.isEmpty()) {
            enricher.accept(items, entities);
        }
        return toResponse(page, items);
    }

    private static <T> PageResponse<T> toResponse(Page<?> page, List<T> items) {
        return new PageResponse<>(
                items,
                new MetaData(
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.getNumber() + 1,
                        page.getSize()
                )
        );
    }
}
