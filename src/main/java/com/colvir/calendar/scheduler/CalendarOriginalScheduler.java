package com.colvir.calendar.scheduler;

import com.colvir.calendar.service.CalendarOriginalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalendarOriginalScheduler {

    private final CalendarOriginalService calendarOriginalService;

    @Scheduled(fixedDelayString = "${app.loadCalendarInterval}")
    public void loadCalendarOriginalAll() {
        calendarOriginalService.loadCalendarOriginalAll();
    }
}
