package com.colvir.calendar.service;

import com.colvir.calendar.config.Config;
import com.colvir.calendar.model.CalendarOriginal;
import com.colvir.calendar.model.RecordStatus;
import com.colvir.calendar.rabbitmq.Producer;
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

    private final CalendarFinalService calendarFinalService;

    private final Producer producer;

    private String loadFromUrl(String country, String year) {

        String calendarUrl = String.format("%s/%s/%s/%s", config.getCalendarUrl(), country, year, FILE_NAME);
        String calendar = null;
        try {
            calendar = IOUtils.toString(URI.create(calendarUrl), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Отправляем сообщение в брокер сообщений, очередь с ошибками по исходному календарю
            producer.sendMessage(producer.getRabbitConfig().getRoutingOriginalErrorKey(),
                    String.format("Error loading calendar. Country: %s, year: %s. Error: %s", country, year, e.getMessage()));
        }
        return calendar;
    }

    private List<CalendarOriginal> findCalendarActual(String country, String year) {

        return calendarOriginalRepository.findAllByCountryAndYearAndIsArchived(country, year, false);
    }

    // Отправка актуальных записей по календарю в архив
    private void setArchiveCalendarsList(List<CalendarOriginal> calendarOriginalList) {

        calendarOriginalList
                .forEach(calendarOriginal -> {
                    calendarOriginal.setIsArchived(true);
                    calendarOriginalRepository.save(calendarOriginal);
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

    private void processCalendarOriginal(String country, String year, String calendarDataActual) {

        // Получение списка неархивных записей по календарю из БД
        List<CalendarOriginal> calendarOriginalCurrentList = findCalendarActual(country, year);

        // Проверим, есть ли актуальная запись по календарю
        if (isNotExistsActualCalendar(calendarOriginalCurrentList, calendarDataActual)) {
            // Если нету
            // Отправляем исходные актуальные записи в архив
            setArchiveCalendarsList(calendarOriginalCurrentList);
            // Загружаем актуальный календарь в БД
            CalendarOriginal actualCalendarOriginal = new CalendarOriginal(country, year, LocalDateTime.now(), RecordStatus.NEW, false, calendarDataActual);
            calendarOriginalRepository.save(actualCalendarOriginal);
            // Отправляем сообщение в брокер сообщений, очередь с информационными сообщениями по исходному календарю
            producer.sendMessage(producer.getRabbitConfig().getRoutingOriginalInfoKey(),
                    String.format("Calendar loaded. Country: %s, year: %s", country, year));
            // Обработка календаря
            calendarFinalService.processCalendarOriginal(calendarDataActual, country);
        }
    }

    @Scheduled(fixedDelayString = "${app.loadCalendarInterval}")
    public void loadCalendarOriginalAll() {

        String[] countryList = config.getCalendarCountryList().split(",");
        String[] yearList = config.getCalendarYearList().split(",");

        for (String country: countryList) {
            for (String year: yearList) {
                loadCalendarOriginalByCountryAndYear(country, year);
            }
        }
    }

    private void loadCalendarOriginalByCountryAndYear(String country, String year) {

        String actualCalendarData = loadFromUrl(country, year);
        if (actualCalendarData != null) {
            processCalendarOriginal(country, year, actualCalendarData);
        }
    }
}
