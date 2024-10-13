package com.colvir.calendar.dto;

import com.colvir.calendar.model.RecordStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CalendarOriginalLastLoadResponse {

    private LocalDateTime dateTime;

    private RecordStatus status;

    private String calendarData;
}
