package com.colvir.calendar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CalendarFinalMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendar_final_month_seq")
    @SequenceGenerator(name = "calendar_final_month_seq", sequenceName = "sequence_calendar_final_month_id", allocationSize = 1)
    private Integer id;

    private String country;

    private Integer year;

    private Integer month;

    private String days;

    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    private RecordStatus status;

    private Boolean isArchived;

    public CalendarFinalMonth(String country, Integer year, Integer month, String days, LocalDateTime dateTime, RecordStatus status, Boolean isArchived) {
        this.country = country;
        this.year = year;
        this.month = month;
        this.days = days;
        this.dateTime = dateTime;
        this.status = status;
        this.isArchived = isArchived;
    }
}
