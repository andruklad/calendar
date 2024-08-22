package com.colvir.calendar.controller;

import com.colvir.calendar.dto.CalendarLoadResult;
import com.colvir.calendar.dto.DayTypeResponse;
import com.colvir.calendar.service.CalendarOriginalService;
import com.colvir.calendar.service.CalendarResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("calendar-resource")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarOriginalService calendarOriginalService;
    private final CalendarResourceService calendarResourceService;

    @GetMapping("get-day-type")
    // Пример формата даты: 2024-08-13
    public DayTypeResponse getDayType(@RequestParam String country, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        return calendarResourceService.getDayType(country, date);
    }

    @GetMapping("load-calendar")
    public List<CalendarLoadResult> loadCalendar() {

        return calendarOriginalService.loadCalendarOriginalAll();
    }
}
