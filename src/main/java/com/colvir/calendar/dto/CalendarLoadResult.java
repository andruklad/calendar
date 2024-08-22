package com.colvir.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarLoadResult {

    private String country;

    private String year;

    private LoadResult loadResult;
}
