package com.colvir.calendar.dto;

import lombok.Data;

import java.time.Month;
import java.util.List;

@Data
public class CalendarDataMonths {

    private List<Month> months;
}
