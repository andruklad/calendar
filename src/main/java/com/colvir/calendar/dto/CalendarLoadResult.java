package com.colvir.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarLoadResult {

    private String country;

    private Integer year;

    private LoadResult loadResult;
}
