package com.raczkowski.app.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MetaData {
    private long totalItems;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
