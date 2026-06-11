package com.raczkowski.app.common.pagination;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class GenericService {
    public static <T> Page<T> paginate(
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Function<Pageable, Page<T>> queryFunction) {

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return queryFunction.apply(pageable);
    }

    public static <T> Page<T> paginate(
            int pageNumber, int pageSize, String sortBy, String sortDirection,
            Supplier<List<T>> listSupplier,
            @Nullable Comparator<T> inMemoryComparator
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(0, pageNumber - 1),
                Math.max(1, Math.min(pageSize, 100)),
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );
        List<T> data = listSupplier.get();
        if (inMemoryComparator != null) {
            return PagingUtils.pageFromListSorted(data, pageable, inMemoryComparator);
        }
        return PagingUtils.pageFromList(data, pageable);
    }
}
