package com.colvir.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarDataStatisticResponse {

    private Integer workdays;

    private Integer holidays;
}
