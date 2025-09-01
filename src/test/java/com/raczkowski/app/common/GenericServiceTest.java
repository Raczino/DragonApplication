package com.raczkowski.app.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenericServiceTest {

    @Mock
    private Function<Pageable, Page<String>> queryFunction;

    @Test
    public void shouldPaginateWithDescSortAndReturnSupplierPage() {
        // Given
        int pageNumber = 1;
        int pageSize = 10;
        String sortBy = "createdAt";
        String sortDir = "DESC";

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(queryFunction.apply(any(Pageable.class))).thenAnswer(inv -> {
            Pageable used = inv.getArgument(0);
            return new PageImpl<>(List.of("A", "B"), used, 2);
        });

        // When
        Page<String> out = GenericService.paginate(pageNumber, pageSize, sortBy, sortDir, queryFunction);

        // Then
        verify(queryFunction).apply(pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();

        int expectedIndex = 0;
        assertEquals(expectedIndex, used.getPageNumber());
        assertEquals(pageSize, used.getPageSize());

        Sort.Order order = used.getSort().getOrderFor(sortBy);
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());

        assertEquals(2, out.getTotalElements());
        assertEquals(1, out.getTotalPages());
        assertEquals(2, out.getContent().size());
        assertEquals("A", out.getContent().get(0));
        assertEquals(used, out.getPageable());
    }

    @Test
    public void shouldPaginateWithAscSort() {
        // Given
        int pageNumber = 1;
        int pageSize = 5;
        String sortBy = "likesCount";
        String sortDir = "ASC";

        when(queryFunction.apply(any(Pageable.class))).thenAnswer(inv -> {
            Pageable used = inv.getArgument(0);
            return new PageImpl<>(List.of(), used, 0);
        });

        // When
        Page<String> out = GenericService.paginate(pageNumber, pageSize, sortBy, sortDir, queryFunction);

        // Then
        Pageable used = out.getPageable();
        assertEquals(0, used.getPageNumber());
        assertEquals(5, used.getPageSize());
        Sort.Order order = used.getSort().getOrderFor("likesCount");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    public void shouldThrowWhenSortDirectionInvalid() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                GenericService.paginate(1, 10, "id", "WRONG_DIRECTION", queryFunction)
        );

        verifyNoInteractions(queryFunction);
    }
}