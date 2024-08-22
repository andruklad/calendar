package com.colvir.calendar.service;

import com.colvir.calendar.dto.CalendarData;
import com.colvir.calendar.model.CalendarFinalMonth;
import com.colvir.calendar.rabbitmq.Producer;
import com.colvir.calendar.repository.CalendarFinalMonthsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalendarFinalService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CalendarFinalMonthsRepository calendarFinalMonthsRepository;

    private final Producer producer;

    private CalendarData parseCalendarOriginal(String calendarDataString) {

        CalendarData calendarData;
        try {
            calendarData = objectMapper.readValue(calendarDataString, CalendarData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return calendarData;
    }


    private List<CalendarFinalMonth> findFinalMonthsActual(CalendarFinalMonth calendarFinalMonthActual) {
        return calendarFinalMonthsRepository.findAllByCountryAndYearAndMonthAndIsArchived(
                calendarFinalMonthActual.getCountry(),
                calendarFinalMonthActual.getYear(),
                calendarFinalMonthActual.getMonth(),
                false
        );
    }

    private boolean calendarFinalMonthsEqualsByDays(CalendarFinalMonth calendarFinalMonthCurrent, CalendarFinalMonth calendarFinalMonthActual) {

        return calendarFinalMonthCurrent.getDays().equals(calendarFinalMonthActual.getDays());
    }

    // Проверка отсутствия актуальных записей по месяцу в календаре
    private boolean isNotExistsActualFinalMonths(List<CalendarFinalMonth> calendarFinalMonthCurrentList, CalendarFinalMonth calendarFinalMonthActual) {

        // Если список пустой, то и нечего проверять - актуальная запись по месяцу отсутствует
        if (calendarFinalMonthCurrentList.isEmpty()) {
            return true;
        }
        Optional<CalendarFinalMonth> calendarFinalMonthsCurrentFilterList = calendarFinalMonthCurrentList.stream()
                .filter(calendarFinalMonth -> (calendarFinalMonthsEqualsByDays(calendarFinalMonth, calendarFinalMonthActual)))
                .findFirst();
        return (calendarFinalMonthsCurrentFilterList.isEmpty());
    }

    // Отправка актуальных записей по месяцу в архив
    private void setArchiveCalendarsList(List<CalendarFinalMonth> calendarFinalMonthList) {

        calendarFinalMonthList
                .forEach(calendarFinalMonth -> {
                    calendarFinalMonth.setIsArchived(true);
                    calendarFinalMonthsRepository.save(calendarFinalMonth);
                });
    }

    // Обработка актуального месяца по календарю
    private void processCalendarFinalMonth(CalendarFinalMonth calendarFinalMonthActual) {

        try {
            // Получение списка неархивных записей по месяцу из БД
            List<CalendarFinalMonth> calendarFinalMonthCurrentList = findFinalMonthsActual(calendarFinalMonthActual);

            // Проверим, есть ли актуальная запись по месяцу
            if (isNotExistsActualFinalMonths(calendarFinalMonthCurrentList, calendarFinalMonthActual)) {
                // Если нету
                // Отправляем исходные актуальные записи в архив
                setArchiveCalendarsList(calendarFinalMonthCurrentList);
                // Загружаем актуальный месяц в БД
                calendarFinalMonthsRepository.save(calendarFinalMonthActual);
                // Отправляем сообщение в брокер сообщений, очередь с информационными сообщениями по месяцам итогового календаря
                producer.sendMessage(producer.getRabbitConfig().getRoutingFinalMonthsInfoKey(),
                        String.format("Calendar months loaded. Country: %s, year: %s. month: %s",
                                calendarFinalMonthActual.getCountry(), calendarFinalMonthActual.getYear(), calendarFinalMonthActual.getMonth()));
            }
        } catch (RuntimeException e) {
            // Отправляем сообщение в брокер сообщений, очередь с сообщениями по ошибкам по месяцам итогового календаря
            producer.sendMessage(producer.getRabbitConfig().getRoutingFinalMonthsErrorKey(),
                    String.format("Calendar month load error. Country: %s, year: %s. month: %s. Error: %s",
                            calendarFinalMonthActual.getCountry(), calendarFinalMonthActual.getYear(), calendarFinalMonthActual.getMonth(), e.getMessage()));
        }
    }

    private void processCalendarData(CalendarData calendarData) {

        // Обработка месяцев
        // Маппинг каждого месяца из списка в сущность
        List<CalendarFinalMonth> calendarFinalMonthList = calendarData.getMonths().stream()
                .map(calendarDataMonth -> new CalendarFinalMonth(
                        calendarData.getCountry(),
                        calendarData.getYear(),
                        calendarDataMonth.getMonth(),
                        calendarDataMonth.getDays(),
                        LocalDateTime.now(),
                        false))
                .toList();

        // Запуск обработки по каждому месяцу
        for (CalendarFinalMonth calendarFinalMonth : calendarFinalMonthList) {
            processCalendarFinalMonth(calendarFinalMonth);
        }
    }

    public void processCalendarOriginal(String calendarDataString, String country) {
        // Преобразование календаря-строки в календарь-объект
        CalendarData calendarData = parseCalendarOriginal(calendarDataString);
        calendarData.setCountry(country);
        // Обработка данных календаря
        processCalendarData(calendarData);
    }
}
