package com.raczkowski.app.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.function.Function;

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
}
