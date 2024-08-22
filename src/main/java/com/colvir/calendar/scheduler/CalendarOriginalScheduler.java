package com.colvir.calendar.scheduler;

import com.colvir.calendar.service.CalendarOriginalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalendarOriginalScheduler {

    private final CalendarOriginalService calendarOriginalService;

    // TODO: 21.08.2024 Расскомментировать 
//    @Scheduled(fixedDelayString = "${app.loadCalendarInterval}")
    public void loadCalendarOriginalAll() {
        calendarOriginalService.loadCalendarOriginalAll();
    }

    @Scheduled(initialDelay = 10*60*1000 /*10 min*/, fixedDelayString = "${app.loadCalendarInterval}")
    public void processCalendarOriginalByNewStatus() {
        calendarOriginalService.processCalendarOriginalByNewStatus();
    }
}
