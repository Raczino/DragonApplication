package com.raczkowski.app.common.offset;

import java.util.List;

public record SliceResponse<T>(
        List<T> items,
        boolean hasNext,
        int nextOffset
) {
}
