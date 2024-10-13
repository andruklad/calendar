package com.colvir.calendar.repository;

import com.colvir.calendar.model.CalendarFinalMonth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarFinalMonthsRepository extends JpaRepository<CalendarFinalMonth, Integer> {

    List<CalendarFinalMonth> findAllByCountryAndYearAndMonthAndIsArchived(String country, Integer year, Integer month, Boolean isArchived);

    CalendarFinalMonth findFirstByCountryAndYearAndMonthAndIsArchived(String country, Integer year, Integer month, Boolean isArchived);
}
