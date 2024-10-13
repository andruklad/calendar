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
public class CalendarFinalStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendar_final_statistic_seq")
    @SequenceGenerator(name = "calendar_final_statistic_seq", sequenceName = "sequence_calendar_final_statistic_id", allocationSize = 1)
    private Integer id;

    private String country;

    private Integer year;

    private Integer workdays;

    private Integer holidays;

    private LocalDateTime dateTime;

    private Boolean isArchived;

    public CalendarFinalStatistic(String country, Integer year, Integer workdays, Integer holidays, LocalDateTime dateTime, Boolean isArchived) {
        this.country = country;
        this.year = year;
        this.workdays = workdays;
        this.holidays = holidays;
        this.dateTime = dateTime;
        this.isArchived = isArchived;
    }
}
