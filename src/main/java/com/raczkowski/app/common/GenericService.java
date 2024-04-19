package com.raczkowski.app.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class GenericService {
    public static <T> Page<T> pagination(JpaRepository<T, Long> repository, int pageNumber, int pageSize, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest
                .of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return repository.findAll(pageable);
    }
}
