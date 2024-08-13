package com.colvir.calendar.dto;

import com.colvir.calendar.model.DayType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DayTypeResponse {

    private Integer dayType;

    private DayType dayTypeName;
}
