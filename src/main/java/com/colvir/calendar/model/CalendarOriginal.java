package com.colvir.calendar.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CalendarOriginal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calendar_original_seq")
    @SequenceGenerator(name = "calendar_original_seq", sequenceName = "sequence_calendar_original_id", allocationSize = 1)
    private Integer id;

    private String country;

    private Integer year;

    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    private RecordStatus status;

    private Boolean isArchived;

    @Type(JsonBinaryType.class)
    @Column(name = "data", columnDefinition = "jsonb")
    private String data;

    public CalendarOriginal(String country, Integer year, LocalDateTime dateTime, RecordStatus status, Boolean isArchived, String data) {
        this.country = country;
        this.year = year;
        this.dateTime = dateTime;
        this.status = status;
        this.isArchived = isArchived;
        this.data = data;
    }
}
