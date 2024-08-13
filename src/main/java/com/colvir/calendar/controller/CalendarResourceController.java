package com.colvir.calendar.controller;

import com.colvir.calendar.dto.DayTypeRequest;
import com.colvir.calendar.dto.DayTypeResponse;
import com.colvir.calendar.service.CalendarResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("calendar-resource")
@RequiredArgsConstructor
public class CalendarResourceController {

    private final CalendarResourceService calendarResourceService;

    @GetMapping("get-day-type")
    // Пример формата даты: 2024-08-13
    public DayTypeResponse getDayType(@RequestParam String country, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        return calendarResourceService.getDayType(country, date);
    }
}
