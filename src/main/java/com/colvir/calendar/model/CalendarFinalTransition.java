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
public class CalendarFinalTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendar_final_transition_seq")
    @SequenceGenerator(name = "calendar_final_transition_seq", sequenceName = "sequence_calendar_final_transition_id", allocationSize = 1)
    private Integer id;

    private String country;

    private Integer year;

    private String dayFrom;

    private String dayTo;

    private LocalDateTime dateTime;

    private Boolean isArchived;

    public CalendarFinalTransition(String country, Integer year, String dayFrom, String dayTo, LocalDateTime dateTime, Boolean isArchived) {
        this.country = country;
        this.year = year;
        this.dayFrom = dayFrom;
        this.dayTo = dayTo;
        this.dateTime = dateTime;
        this.isArchived = isArchived;
    }
}
