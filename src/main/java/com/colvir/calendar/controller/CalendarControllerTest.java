package com.colvir.calendar.controller;

import com.colvir.calendar.service.CalendarServiceTest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("calendar-test")
@RequiredArgsConstructor
public class CalendarControllerTest {

    private final CalendarServiceTest calendarServiceTest;

    @GetMapping("load-calendar")
    public String loadCalendar() {

        return calendarServiceTest.loadCalendar();
    }
}
