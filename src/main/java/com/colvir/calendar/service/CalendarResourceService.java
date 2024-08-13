package com.colvir.calendar.service;

import com.colvir.calendar.dto.DayTypeRequest;
import com.colvir.calendar.dto.DayTypeResponse;
import com.colvir.calendar.entity.CalendarFinalMonth;
import com.colvir.calendar.entity.DayType;
import com.colvir.calendar.repository.CalendarFinalMonthsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalendarResourceService {

    private final CalendarFinalMonthsRepository calendarFinalMonthsRepository;

    private final DayTypeService dayTypeService;

    public DayTypeResponse getDayType(DayTypeRequest dayTypeRequest) {

        // Номер дня запрашиваемой даты в месяце
        Integer dayOfMonth = dayTypeRequest.getDate().getDayOfMonth();
        // Получение строки дней по месяцу запрашиваемой даты
        Integer year = dayTypeRequest.getDate().getYear();
        Integer month = dayTypeRequest.getDate().getMonthValue();
        CalendarFinalMonth calendarFinalMonth = calendarFinalMonthsRepository.findFirstByCountryAndYearAndMonthAndIsArchived(
                dayTypeRequest.getCountry(), year, month,false);
        String days = calendarFinalMonth.getDays();
        // Расчет типа дня
        DayType dayType = dayTypeService.calcDayType(dayOfMonth, days);

        return new DayTypeResponse(dayType.getValue(), dayType);
    }
}
