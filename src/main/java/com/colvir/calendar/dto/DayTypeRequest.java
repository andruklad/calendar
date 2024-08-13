package com.colvir.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DayTypeRequest {

    private String country;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}
