package com.colvir.calendar.service;

import com.colvir.calendar.entity.DayType;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DayTypeService {

    public DayType calcDayType(Integer dayOfMonth, String calendarMonthDays) {

        DayType result;

        // Паттерн для поиска дня по строке дней
        String patternString = String.format("%s{1}[*+]?", dayOfMonth);
        Pattern DIGIT_CURRENT_DAY = Pattern.compile(patternString);

        Matcher m = DIGIT_CURRENT_DAY.matcher(calendarMonthDays);

        String foundDay = "";
        // Поиск дня по строке дней
        if (m.find()) {
            foundDay = calendarMonthDays.substring(m.start(), m.end());
        }
        // Обычный рабочий день
        if (foundDay.equals(""))
            result = DayType.WORKING_DAY;
        // Сокращенный рабочий день
        else if (foundDay.equals(String.format("%s*", dayOfMonth)))
            result = DayType.SHORTENED_WORKING_DAY;
        // Выходной день
        else
            result = DayType.DAY_OFF;

        return result;
    }
}
