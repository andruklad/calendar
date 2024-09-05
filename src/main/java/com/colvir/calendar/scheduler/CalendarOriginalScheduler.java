package com.colvir.calendar.scheduler;

import com.colvir.calendar.service.CalendarOriginalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalendarOriginalScheduler {

    private final CalendarOriginalService calendarOriginalService;

    @Scheduled(initialDelayString = "#{${app.loadCalendarInitialDelay} * 60000}", fixedDelayString = "#{${app.loadCalendarInterval} * 60000}")
    public void loadCalendarOriginalAll() {
        calendarOriginalService.loadCalendarOriginalAll();
    }

    @Scheduled(initialDelayString = "#{${app.processCalendarInitialDelay} * 60000}", fixedDelayString = "#{${app.processCalendarInterval} * 60000}")
    public void processCalendarOriginalByNewStatus() {
        calendarOriginalService.processCalendarOriginalByNewStatus();
    }
}
