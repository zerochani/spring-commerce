package com.example.commerce_mvp.application.common.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SliceResponse<T> {
    private final List<T> content;
    private final boolean hasNext;
    private final Long nextCursor;

    public SliceResponse(List<T> content, boolean hasNext, Long nextCursor){
        this.content = content;
        this.hasNext = hasNext;
        this.nextCursor = nextCursor;
    }
}
