package com.colvir.calendar.service;

import com.colvir.calendar.dto.DayTypeResponse;
import com.colvir.calendar.model.CalendarFinalMonth;
import com.colvir.calendar.model.DayType;
import com.colvir.calendar.repository.CalendarFinalMonthsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CalendarResourceService {

    private final CalendarFinalMonthsRepository calendarFinalMonthsRepository;

    private final DayTypeService dayTypeService;

    public DayTypeResponse getDayType(String country, LocalDate date) {

        // Номер дня запрашиваемой даты в месяце
        Integer dayOfMonth = date.getDayOfMonth();
        // Получение строки дней по месяцу запрашиваемой даты
        Integer year = date.getYear();
        Integer month = date.getMonthValue();
        CalendarFinalMonth calendarFinalMonth =
                calendarFinalMonthsRepository.findFirstByCountryAndYearAndMonthAndIsArchived(country, year, month,false);
        String days = calendarFinalMonth.getDays();
        // Расчет типа дня
        DayType dayType = dayTypeService.calcDayType(dayOfMonth, days);

        return new DayTypeResponse(dayType.getValue(), dayType);
    }
}
