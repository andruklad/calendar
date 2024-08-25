package com.colvir.calendar.repository;

import com.colvir.calendar.model.CalendarOriginal;
import com.colvir.calendar.model.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarOriginalRepository extends JpaRepository<CalendarOriginal, Integer> {

    List<CalendarOriginal> findAllByCountryAndYearAndIsArchived(String country, Integer year, boolean isArchived);

    List<CalendarOriginal> findAllByCountryAndYearAndIsArchivedAndStatus(String country, Integer year, boolean isArchived, RecordStatus status);

    CalendarOriginal findFirstByCountryAndYearOrderByDateTimeDesc(String country, Integer year);
}
