package com.colvir.calendar.service;

import com.colvir.calendar.dto.CalendarDataStatisticResponse;
import com.colvir.calendar.dto.CalendarOriginalLastLoadResponse;
import com.colvir.calendar.dto.DayTypeResponse;
import com.colvir.calendar.dto.TransitionResponse;
import com.colvir.calendar.exception.LastUpdateNotFoundException;
import com.colvir.calendar.exception.MonthDataNotFoundException;
import com.colvir.calendar.exception.StatisticDataNotFoundException;
import com.colvir.calendar.model.*;
import com.colvir.calendar.repository.CalendarFinalMonthsRepository;
import com.colvir.calendar.repository.CalendarFinalStatisticRepository;
import com.colvir.calendar.repository.CalendarFinalTransitionsRepository;
import com.colvir.calendar.repository.CalendarOriginalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarResourceService {

    private final CalendarFinalMonthsRepository calendarFinalMonthsRepository;

    private final DayTypeService dayTypeService;

    private final CalendarFinalTransitionsRepository calendarFinalTransitionsRepository;

    private final CalendarOriginalRepository calendarOriginalRepository;

    private final CalendarFinalStatisticRepository calendarFinalStatisticRepository;

    public DayTypeResponse getDayType(String country, LocalDate date) {

        // Номер дня запрашиваемой даты в месяце
        Integer dayOfMonth = date.getDayOfMonth();
        // Получение строки дней по месяцу запрашиваемой даты
        Integer year = date.getYear();
        Integer month = date.getMonthValue();
        CalendarFinalMonth calendarFinalMonth =
                calendarFinalMonthsRepository.findFirstByCountryAndYearAndMonthAndIsArchived(country, year, month,false);

        // Если не нашли - генерируем исключение
        if (calendarFinalMonth == null) {
            throw new MonthDataNotFoundException(String.format("Не найдены актуальные данные месяца по стране %s, году %s и месяцу %s", country, year, month));
        }

        String days = calendarFinalMonth.getDays();
        // Расчет типа дня
        DayType dayType = dayTypeService.calcDayType(dayOfMonth, days);

        return new DayTypeResponse(dayType.getValue(), dayType);
    }

    public List<TransitionResponse> getTransitions(String country, Integer year) {

        List<CalendarFinalTransition> calendarFinalTransitionList = calendarFinalTransitionsRepository.findAllByCountryAndYearAndIsArchived(country, year, false);
        List<TransitionResponse> transitionResponseList = calendarFinalTransitionList.stream()
                .map(calendarFinalTransition -> (new TransitionResponse(calendarFinalTransition.getDayFrom(), calendarFinalTransition.getDayTo())))
                .toList();
        return transitionResponseList;
    }

    public CalendarDataStatisticResponse getStatistic(String country, Integer year) {

        CalendarFinalStatistic calendarFinalStatistic = calendarFinalStatisticRepository.findFirstByCountryAndYearAndIsArchived(
                country,
                year,
                false);

        // Если не нашли - генерируем исключение
        if (calendarFinalStatistic == null) {
            throw new StatisticDataNotFoundException(String.format("Не найдены актуальные данные статистики по стране %s и году %s", country, year));
        }

        CalendarDataStatisticResponse calendarDataStatisticResponse = new CalendarDataStatisticResponse(
                calendarFinalStatistic.getWorkdays(),
                calendarFinalStatistic.getHolidays());
        return calendarDataStatisticResponse;
    }

    public CalendarOriginalLastLoadResponse getLastUpdate(String country, Integer year) {

        // Ищем последнюю запись по календарю
        CalendarOriginal calendarOriginal = calendarOriginalRepository.findFirstByCountryAndYearOrderByDateTimeDesc(country, year);

        // Если не нашли - генерируем исключение
        if (calendarOriginal == null) {
            throw new LastUpdateNotFoundException(String.format("Не найдено последнее обновление календаря по стране %s и году %s", country, year));
        }

        CalendarOriginalLastLoadResponse calendarOriginalLastLoadResponse = new CalendarOriginalLastLoadResponse(
                calendarOriginal.getDateTime(),
                calendarOriginal.getStatus(),
                calendarOriginal.getData()
        );
        return calendarOriginalLastLoadResponse;
    }
}
