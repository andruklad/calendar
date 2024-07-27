package com.colvir.calendar.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarOriginal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendar_original_seq")
    @SequenceGenerator(name = "calendar_original_seq", sequenceName = "sequence_calendar_original_id", allocationSize = 1)
    private Integer id;

    private String country;

    private String year;

    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    private CalendarOriginalStatus status;

    private Boolean isArchived;

    @Type(JsonBinaryType.class)
    @Column(name = "data", columnDefinition = "jsonb")
    private String data;

    public CalendarOriginal(String country, String year, LocalDateTime dateTime, CalendarOriginalStatus status, Boolean isArchived, String data) {
        this.country = country;
        this.year = year;
        this.dateTime = dateTime;
        this.status = status;
        this.isArchived = isArchived;
        this.data = data;
    }
}
