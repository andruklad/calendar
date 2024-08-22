package com.colvir.calendar.dto;

public enum LoadResult {

    SUCCESS(1),
    LOAD_FAIL(2),
    PROCESS_FAIL(3);

    private final int value;

    LoadResult(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
