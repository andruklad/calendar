package com.colvir.calendar.dto;

import lombok.Data;

@Data
public class CalendarDataStatistic {

    private Integer workdays;

    private Integer holidays;

    private Integer hours40;

    private Double hours36;

    private Double hours24;
}
