package com.colvir.calendar.service;

import com.colvir.calendar.config.Config;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

// TODO: 25.07.2024 Тестовый рабочий вариант, пока не удалять
@Service
@AllArgsConstructor
public class CalendarServiceTest {

    private final Config config;

    public String loadCalendar() {

        String calendarUrlRu2024 = config.getCalendarUrl() + "/ru/2024/calendar.json";

        String jsonCalendar;
        try {
            jsonCalendar = IOUtils.toString(URI.create(calendarUrlRu2024), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new JSONObject(jsonCalendar).toString();
    }
}
