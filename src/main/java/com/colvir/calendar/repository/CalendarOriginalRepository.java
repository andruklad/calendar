package com.colvir.calendar.repository;

import com.colvir.calendar.model.CalendarOriginal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarOriginalRepository extends JpaRepository<CalendarOriginal, Integer> {

    List<CalendarOriginal> findAllByCountryAndYearAndIsArchived(String country, String year, boolean isArchived);
}
