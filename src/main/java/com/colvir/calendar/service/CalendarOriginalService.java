package com.colvir.calendar.service;

import com.colvir.calendar.config.Config;
import com.colvir.calendar.config.RabbitConfig;
import com.colvir.calendar.dto.CalendarLoadResult;
import com.colvir.calendar.dto.LoadResult;
import com.colvir.calendar.model.CalendarOriginal;
import com.colvir.calendar.model.RecordStatus;
import com.colvir.calendar.rabbitmq.Producer;
import com.colvir.calendar.repository.CalendarOriginalRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalendarOriginalService {

    private final Config config;

    private final RabbitConfig rabbitConfig;

    private final CalendarOriginalRepository calendarOriginalRepository;

    private final CalendarFinalService calendarFinalService;

    private final Producer producer;

    private final LoadSourceDataService loadSourceDataService;

    // TODO: 21.08.2024 Переделать
    private String loadFromUrl(String country, String year) {

        String result = null;
        try {
            result = loadSourceDataService.loadFromUrl(country, year);
        } catch (IOException e) {
            // Отправляем сообщение в брокер сообщений, очередь с ошибками по исходному календарю
            producer.sendMessage(rabbitConfig.getRoutingOriginalErrorKey(),
                    String.format("Error loading calendar. Country: %s, year: %s. Error: %s", country, year, e.getMessage()));
        }
        return result;
    }

    private List<CalendarOriginal> findCalendarActual(String country, String year) {

        return calendarOriginalRepository.findAllByCountryAndYearAndIsArchived(country, year, false);
    }

    // Отправка актуальных записей по календарю в архив
    private void setArchiveCalendarsList(List<CalendarOriginal> calendarOriginalList) {

        if (calendarOriginalList != null) {
            calendarOriginalList
                    .forEach(calendarOriginal -> {
                        calendarOriginal.setIsArchived(true);
                        calendarOriginalRepository.save(calendarOriginal);
                    });
        }
    }

    // Пометить запись как обработанную
    private void setProcessedCalendarOriginal(CalendarOriginal calendarOriginal) {

        calendarOriginal.setStatus(RecordStatus.PROCESSED);
        calendarOriginalRepository.save(calendarOriginal);
    }

    private boolean isNotExistsActualCalendar(List<CalendarOriginal> calendarOriginalList, String actualCalendarData) {

        // Если список отсутствует или пустой, то и нечего проверять - актуальный календарь отсутствует
        if (calendarOriginalList == null || calendarOriginalList.isEmpty()) {
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

    private LoadResult processCalendarOriginal(String country, String year, String calendarDataActual) {

        LoadResult result;
        try {
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
                processCalendarOriginalToFinal(country, calendarDataActual, actualCalendarOriginal);
                // Отправляем сообщение в брокер сообщений, очередь с информационными сообщениями по исходному календарю
                producer.sendMessage(rabbitConfig.getRoutingOriginalInfoKey(),
                        String.format("Calendar processed. Country: %s, year: %s", country, year));
            }
            result = LoadResult.SUCCESS;
        } catch (RuntimeException e) {
            result = LoadResult.PROCESS_FAIL;
            // Отправляем сообщение в брокер сообщений, очередь с ошибками по исходному календарю
            producer.sendMessage(rabbitConfig.getRoutingOriginalErrorKey(),
                    String.format("Error processing calendar. Country: %s, year: %s. Error: %s", country, year, e.getMessage()));
        }
        return result;
    }

    private void processCalendarOriginalToFinal(String country, String calendarDataActual, CalendarOriginal actualCalendarOriginal) {

        // Обработка календаря
        calendarFinalService.processCalendarOriginal(calendarDataActual, country);
        // Пометить запись как обработанную
        setProcessedCalendarOriginal(actualCalendarOriginal);
    }

    public void processCalendarOriginalByNewStatus() {

        String[] countryList = config.getCalendarCountryList().split(",");
        String[] yearList = config.getCalendarYearList().split(",");
        for (String country: countryList) {
            for (String year: yearList) {
                // Получение неархивных необработанных записей
                List<CalendarOriginal> calendarOriginalList = calendarOriginalRepository.findAllByCountryAndYearAndIsArchivedAndStatus(country, year, false, RecordStatus.NEW);
                for (CalendarOriginal calendarOriginal: calendarOriginalList) {
                    processCalendarOriginalToFinal(country, calendarOriginal.getData(), calendarOriginal);
                }
            }
        }
    }

    private LoadResult loadCalendarOriginalByCountryAndYear(String country, String year) {

        String actualCalendarData = loadFromUrl(country, year);
        if (actualCalendarData != null)
            return processCalendarOriginal(country, year, actualCalendarData);
        else
            return LoadResult.LOAD_FAIL;
    }

    public List<CalendarLoadResult> loadCalendarOriginalAll() {

        List<CalendarLoadResult> calendarLoadResultList = new ArrayList<>();
        String[] countryList = config.getCalendarCountryList().split(",");
        String[] yearList = config.getCalendarYearList().split(",");

        for (String country: countryList) {
            for (String year: yearList) {
                LoadResult loadResult = loadCalendarOriginalByCountryAndYear(country, year);
                CalendarLoadResult calendarLoadResult = new CalendarLoadResult(country, year, loadResult);
                calendarLoadResultList.add(calendarLoadResult);
            }
        }
        return calendarLoadResultList;
    }
}
