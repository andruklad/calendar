package com.colvir.calendar.service;

import com.colvir.calendar.config.Config;
import com.colvir.calendar.config.RabbitConfig;
import com.colvir.calendar.dto.CalendarLoadResult;
import com.colvir.calendar.dto.LoadResult;
import com.colvir.calendar.rabbitmq.Producer;
import com.colvir.calendar.repository.CalendarOriginalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CalendarOriginalService.class,
        Producer.class,
        Config.class,
        RabbitConfig.class
})
@SpringBootTest
@PropertySource(value = "classpath:application.yml")
public class CalendarOriginalServiceTest {

    @Autowired
    private CalendarOriginalService calendarOriginalService;

    @MockBean
    private CalendarOriginalRepository calendarOriginalRepository;

    @MockBean
    private LoadSourceDataService loadSourceDataService;

    @MockBean
    private CalendarFinalService calendarFinalService;

    @Test
    void loadCalendarOriginalAll_success() throws IOException {

        //Подготовка входных данных

        //Подготовка ожидаемого результата
        List<CalendarLoadResult> expectedCalendarLoadResultList = new ArrayList<>();
        expectedCalendarLoadResultList.add(new CalendarLoadResult("ru", "2024", LoadResult.SUCCESS));
        String calendarDataString = TestUtils.getCalendarDataString();
        when(loadSourceDataService.loadFromUrl("ru", "2024")).thenReturn(calendarDataString);


        // TODO: 21.08.2024 Удалить закомментированный код
//        List<CalendarOriginal> calendarOriginalList = new ArrayList<>();
//        when(calendarOriginalRepository.findAllByCountryAndYearAndIsArchived(any(), any(), any())).thenReturn(calendarOriginalList);
//        CalendarOriginal calendarOriginal = new CalendarOriginal();
//        when(calendarOriginalRepository.save(any())).thenReturn(null);
//        when(calendarOriginalRepository.findAllByCountryAndYearAndIsArchived(anyString(), anyString(), anyBoolean())).thenReturn(null);
//        doNothing().when(calendarOriginalRepository).findAllByCountryAndYearAndIsArchived(anyString(), anyString(), anyBoolean());
//        doNothing().when(calendarFinalService).processCalendarOriginal(anyString(), anyString());

        // ArgumentCaptor

        //Начало теста
        List<CalendarLoadResult> actualCalendarLoadResultList = calendarOriginalService.loadCalendarOriginalAll();
        assertEquals(expectedCalendarLoadResultList, actualCalendarLoadResultList);
//        Assertions.assertDoesNotThrow(() -> calendarOriginalService.loadCalendarOriginalAll());

        verify(calendarOriginalRepository, times(2)).save(any());
        verify(calendarOriginalRepository).findAllByCountryAndYearAndIsArchived("ru", "2024", false);
        verifyNoMoreInteractions(calendarOriginalRepository);
    }
}
