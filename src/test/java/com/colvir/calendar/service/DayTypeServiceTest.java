package com.colvir.calendar.service;

import com.colvir.calendar.model.DayType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        DayTypeService.class
})
public class DayTypeServiceTest {

    @Autowired
    DayTypeService dayTypeService;

    @Test
    public void calcDayType_success() {

        //Подготовка входных данных
        Integer dayOfMonth = 26;
        String calendarMonthDays = "3,4,10,11,17,18,24,25,31";

        //Подготовка ожидаемого результата
        DayType requiredDayType = DayType.WORKING_DAY;

        //Начало теста
        DayType actualDayType = dayTypeService.calcDayType(dayOfMonth, calendarMonthDays);
        assertEquals(requiredDayType, actualDayType);
    }
}
