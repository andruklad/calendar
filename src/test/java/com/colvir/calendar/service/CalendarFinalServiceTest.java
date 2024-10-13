package com.colvir.calendar.service;

import com.colvir.calendar.config.Config;
import com.colvir.calendar.config.RabbitConfig;
import com.colvir.calendar.rabbitmq.Producer;
import com.colvir.calendar.repository.CalendarFinalMonthsRepository;
import com.colvir.calendar.repository.CalendarFinalStatisticRepository;
import com.colvir.calendar.repository.CalendarFinalTransitionsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CalendarFinalService.class,
        Producer.class,
        Config.class,
        RabbitConfig.class
})
@SpringBootTest
public class CalendarFinalServiceTest {

    @Autowired
    private CalendarFinalService calendarFinalService;

    @MockBean
    private CalendarFinalMonthsRepository calendarFinalMonthsRepository;

    @MockBean
    private CalendarFinalTransitionsRepository calendarFinalTransitionsRepository;

    @MockBean
    private CalendarFinalStatisticRepository calendarFinalStatisticRepository;

    @MockBean Producer producer;

    @Test
    void processCalendarOriginal_success() {

        //Подготовка входных данных
        String calendarDataString = TestUtils.getCalendarDataString();
        String country = "ru";

        //Подготовка ожидаемого результата

        //Начало теста
        Assertions.assertDoesNotThrow(() -> calendarFinalService.processCalendarOriginal(calendarDataString, country));
        // Обработка месяцев
        verify(calendarFinalMonthsRepository, times(12)).findAllByCountryAndYearAndMonthAndIsArchived(any(), any(), any(), any());
        verify(calendarFinalMonthsRepository, times(12)).save(any());
        verifyNoMoreInteractions(calendarFinalMonthsRepository);
        // Обработка переносов
        verify(calendarFinalTransitionsRepository).findAllByCountryAndYearAndIsArchived(any(), any(), any());
        verify(calendarFinalTransitionsRepository, times(5)).save(any());
        verifyNoMoreInteractions(calendarFinalTransitionsRepository);
        // Обработка статистики
        verify(calendarFinalStatisticRepository).findAllByCountryAndYearAndIsArchived(any(), any(), any());
        verify(calendarFinalStatisticRepository).save(any());
        verifyNoMoreInteractions(calendarFinalStatisticRepository);
    }
}
