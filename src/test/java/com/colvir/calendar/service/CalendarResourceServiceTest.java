package com.colvir.calendar.service;

import com.colvir.calendar.config.Config;
import com.colvir.calendar.config.RabbitConfig;
import com.colvir.calendar.dto.*;
import com.colvir.calendar.model.*;
import com.colvir.calendar.rabbitmq.Producer;
import com.colvir.calendar.repository.CalendarFinalMonthsRepository;
import com.colvir.calendar.repository.CalendarFinalStatisticRepository;
import com.colvir.calendar.repository.CalendarFinalTransitionsRepository;
import com.colvir.calendar.repository.CalendarOriginalRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CalendarResourceService.class,
        Producer.class,
        Config.class,
        RabbitConfig.class
})
@SpringBootTest
public class CalendarResourceServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CalendarResourceService calendarResourceService;

    @MockBean
    private DayTypeService dayTypeService;

    @MockBean
    private CalendarOriginalRepository calendarOriginalRepository;

    @MockBean
    private CalendarFinalMonthsRepository calendarFinalMonthsRepository;

    @MockBean
    private CalendarFinalTransitionsRepository calendarFinalTransitionsRepository;

    @MockBean
    private CalendarFinalStatisticRepository calendarFinalStatisticRepository;

    @Test
    void getDayType_success() {

        //Подготовка входных данных
        String country = "ru";
        Integer year = 2024;
        Integer month = 8;
        Integer dayOfMonth = 26;
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDate date = LocalDate.of(year, month, dayOfMonth);

        //Подготовка ожидаемого результата
        String calendarDataString = TestUtils.getCalendarDataString();
        CalendarData calendarData;
        try {
            calendarData = objectMapper.readValue(calendarDataString, CalendarData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String days = calendarData.getMonths().get(7).getDays(); // Август
        CalendarFinalMonth calendarFinalMonth = new CalendarFinalMonth(country, year, month, days, localDateTime, false);
        when(calendarFinalMonthsRepository.findFirstByCountryAndYearAndMonthAndIsArchived(country, year, month, false)).thenReturn(calendarFinalMonth);
        DayType dayType = DayType.WORKING_DAY;
        DayTypeResponse expectedDayTypeResponse = new DayTypeResponse(dayType.getValue(), dayType);
        when(dayTypeService.calcDayType(dayOfMonth, days)).thenReturn(DayType.WORKING_DAY);

        //Начало теста
        DayTypeResponse actualDayTypeResponse = calendarResourceService.getDayType(country, date);
        assertEquals(expectedDayTypeResponse, actualDayTypeResponse);
        verify(calendarFinalMonthsRepository).findFirstByCountryAndYearAndMonthAndIsArchived(country, year, month, false);
        verifyNoMoreInteractions(calendarFinalMonthsRepository);
    }

    @Test
    void getTransitions_success() {

        //Подготовка входных данных
        String country = "ru";
        Integer year = 2024;

        //Подготовка ожидаемого результата
        String dayFrom = "04.27";
        String dayTo = "04.29";
        LocalDateTime localDateTime = LocalDateTime.now();
        CalendarFinalTransition calendarFinalTransition = new CalendarFinalTransition(country, year, dayFrom, dayTo, localDateTime, false);
        List<CalendarFinalTransition> calendarFinalTransitionList = new ArrayList<>(List.of(calendarFinalTransition));
        when(calendarFinalTransitionsRepository.findAllByCountryAndYearAndIsArchived(country, year, false)).thenReturn(calendarFinalTransitionList);

        TransitionResponse requiredTransitionResponse = new TransitionResponse(dayFrom, dayTo);
        List<TransitionResponse> requiredTransitionResponseList = new ArrayList<>(List.of(requiredTransitionResponse));

        //Начало теста
        List<TransitionResponse> actualTransitionResponseList = calendarResourceService.getTransitions(country, year);
        assertEquals(requiredTransitionResponseList, actualTransitionResponseList);
        verify(calendarFinalTransitionsRepository).findAllByCountryAndYearAndIsArchived(country, year, false);
        verifyNoMoreInteractions(calendarFinalTransitionsRepository);
    }

    @Test
    void getStatistic_success() {

        //Подготовка входных данных
        String country = "ru";
        Integer year = 2024;

        //Подготовка ожидаемого результата
        Integer workdays = 248;
        Integer holidays = 118;
        LocalDateTime localDateTime = LocalDateTime.now();
        CalendarDataStatisticResponse requiredCalendarDataStatisticResponse = new CalendarDataStatisticResponse(workdays, holidays);
        CalendarFinalStatistic calendarFinalStatistic = new CalendarFinalStatistic(country, year, workdays, holidays, localDateTime, false);
        when(calendarFinalStatisticRepository.findFirstByCountryAndYearAndIsArchived(country, year, false)).thenReturn(calendarFinalStatistic);

        //Начало теста
        CalendarDataStatisticResponse actualCalendarDataStatisticResponse = calendarResourceService.getStatistic(country, year);
        assertEquals(requiredCalendarDataStatisticResponse, actualCalendarDataStatisticResponse);
        verify(calendarFinalStatisticRepository).findFirstByCountryAndYearAndIsArchived(country, year, false);
        verifyNoMoreInteractions(calendarFinalStatisticRepository);
    }

    @Test
    void getLastUpdate_success() {

        //Подготовка входных данных
        String country = "ru";
        Integer year = 2024;

        //Подготовка ожидаемого результата
        String calendarDataString = TestUtils.getCalendarDataString();
        LocalDateTime localDateTime = LocalDateTime.now();

        CalendarOriginal calendarOriginal = new CalendarOriginal(country, year, localDateTime, RecordStatus.PROCESSED, false, calendarDataString);
        when(calendarOriginalRepository.findFirstByCountryAndYearOrderByDateTimeDesc(country, year)).thenReturn(calendarOriginal);
        CalendarOriginalLastLoadResponse requiredCalendarOriginalLastLoadResponse = new CalendarOriginalLastLoadResponse(
                calendarOriginal.getDateTime(),
                calendarOriginal.getStatus(),
                calendarDataString);

        //Начало теста
        CalendarOriginalLastLoadResponse actualCalendarOriginalLastLoadResponse = calendarResourceService.getLastUpdate(country, year);
        assertEquals(requiredCalendarOriginalLastLoadResponse, actualCalendarOriginalLastLoadResponse);
        verify(calendarOriginalRepository).findFirstByCountryAndYearOrderByDateTimeDesc(country, year);
        verifyNoMoreInteractions(calendarOriginalRepository);
    }
}
