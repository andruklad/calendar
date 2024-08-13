package com.colvir.calendar.entity;

public enum DayType {

    DAY_OFF(1),
    SHORTENED_WORKING_DAY(2),
    WORKING_DAY(3);

    private final int value;

    DayType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
