package com.colvir.calendar.dto;

import lombok.Data;

import java.util.List;

@Data
public class CalendarData {

    private String country;

    private Integer year;

    private List<CalendarDataMonth> months;

    private List<CalendarDataTransition> transitions;

    private CalendarDataStatistic statistic;
}
