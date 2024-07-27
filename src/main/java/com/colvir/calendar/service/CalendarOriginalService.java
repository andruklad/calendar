package com.colvir.calendar.service;

import com.colvir.calendar.config.Config;
import com.colvir.calendar.entity.CalendarOriginalStatus;
import com.colvir.calendar.entity.CalendarOriginal;
import com.colvir.calendar.repository.CalendarOriginalRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CalendarOriginalService {

    private final Config config;

    private final String FILE_NAME = "calendar.json";

    private final CalendarOriginalRepository calendarOriginalRepository;

    private String loadFromUrl(String country, String year) {

        String calendarUrl = String.format("%s/%s/%s/%s", config.getCalendarUrl(), country, year, FILE_NAME);
        String calendar;
        try {
            calendar = IOUtils.toString(URI.create(calendarUrl), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return calendar;
    }

    private List<CalendarOriginal> findActualCalendar(String country, String year) {

        List<CalendarOriginal> calendarOriginalList = calendarOriginalRepository.findAllByCountryAndYearAndIsArchived(country, year, false);
        return calendarOriginalList;
    }

    private void setArchiveCalendarsList(List<CalendarOriginal> calendarOriginalList) {

        calendarOriginalList.stream()
                .forEach(cl -> {
                    cl.setIsArchived(true);
                    calendarOriginalRepository.save(cl);
                });
    }

    private boolean isNotExistsActualCalendar(List<CalendarOriginal> calendarOriginalList, String actualCalendarData) {

        // Если список пустой, то и нечего проверять - актуальный календарь отсутствует
        if (calendarOriginalList.isEmpty()) {
            return true;
        }
        JSONObject actualCalendarDataJson = new JSONObject(actualCalendarData);
        Optional<CalendarOriginal> actualCalendars = calendarOriginalList.stream()
//                .peek(cl -> {
//                    String s = new JSONObject(cl.getData()).toString();
//                    System.out.println(cl.getData());
//                    System.out.println(s);
//                    System.out.println(actualCalendarData);
//                    System.out.println(actualCalendarDataJson);

//                    System.out.println(new JSONObject(cl.getData()));
//                    System.out.println(actualCalendarDataJson);
//                })
                // TODO: 27.07.2024 Подумать над оптимизацией
                .filter(cl -> (new JSONObject(cl.getData())).toString().equals(actualCalendarDataJson.toString()))
                .findFirst();
        return (actualCalendars.isEmpty());
    }

    @Scheduled(fixedDelayString = "${app.loadCalendarInterval}")
    public void loadAndProcessCalendar() {

        String country = "ru";
        String year = "2024";

        String actualCalendarData = loadFromUrl(country, year);

        // Получение списка неархивных записей по календарю из БД
        List<CalendarOriginal> calendarOriginalList = findActualCalendar(country, year);

        // Проверим, есть ли актуальная запись по календарю
        if (isNotExistsActualCalendar(calendarOriginalList, actualCalendarData)) {
            // Если нету
            // Отправляем исходные актуальные в архив
            setArchiveCalendarsList(calendarOriginalList);
            // Загружаем актуальный календарь в БД
            CalendarOriginal actualCalendarOriginal = new CalendarOriginal(country, year, LocalDateTime.now(), CalendarOriginalStatus.NEW, false, actualCalendarData);
            calendarOriginalRepository.save(actualCalendarOriginal);
        }
    }
}
