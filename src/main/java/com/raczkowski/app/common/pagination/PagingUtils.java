package com.raczkowski.app.common.pagination;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor
public final class PagingUtils {
    public static <T> Page<T> pageFromList(List<T> all, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= all.size()) return new PageImpl<>(List.of(), pageable, all.size());
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<T> content = all.subList(start, end);
        return new PageImpl<>(content, pageable, all.size());
    }

    public static <T> Page<T> pageFromListSorted(List<T> all, Pageable pageable, Comparator<T> cmp) {
        List<T> copy = new ArrayList<>(all);
        copy.sort(cmp);
        return pageFromList(copy, pageable);
    }
}
