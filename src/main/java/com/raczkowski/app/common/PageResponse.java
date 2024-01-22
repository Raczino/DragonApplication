package com.raczkowski.app.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class PageResponse<T> {
    private List<T> items;
    private MetaData meta;
}
