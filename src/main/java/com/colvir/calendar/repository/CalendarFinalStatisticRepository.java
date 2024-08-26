package com.colvir.calendar.repository;

import com.colvir.calendar.model.CalendarFinalStatistic;
import com.colvir.calendar.model.CalendarFinalTransition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarFinalStatisticRepository extends JpaRepository<CalendarFinalStatistic, Integer> {

    List<CalendarFinalStatistic> findAllByCountryAndYearAndIsArchived(String country, Integer year, Boolean isArchived);
}
