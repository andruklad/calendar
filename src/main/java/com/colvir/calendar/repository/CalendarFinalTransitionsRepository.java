package com.colvir.calendar.repository;

import com.colvir.calendar.model.CalendarFinalTransition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarFinalTransitionsRepository extends JpaRepository<CalendarFinalTransition, Integer> {

    List<CalendarFinalTransition> findAllByCountryAndYearAndIsArchived(String country, Integer year, Boolean isArchived);
}
