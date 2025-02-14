package com.colvir.calendar.service;

import com.colvir.calendar.config.RabbitConfig;
import com.colvir.calendar.dto.CalendarData;
import com.colvir.calendar.model.CalendarFinalMonth;
import com.colvir.calendar.model.CalendarFinalStatistic;
import com.colvir.calendar.model.CalendarFinalTransition;
import com.colvir.calendar.rabbitmq.Producer;
import com.colvir.calendar.repository.CalendarFinalMonthsRepository;
import com.colvir.calendar.repository.CalendarFinalStatisticRepository;
import com.colvir.calendar.repository.CalendarFinalTransitionsRepository;
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

    private final RabbitConfig rabbitConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CalendarFinalMonthsRepository calendarFinalMonthsRepository;

    private final CalendarFinalTransitionsRepository calendarFinalTransitionsRepository;

    private final CalendarFinalStatisticRepository calendarFinalStatisticRepository;

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
    private void processCalendarFinalMonthList(List<CalendarFinalMonth> calendarFinalMonthActualList) {

        if (calendarFinalMonthActualList == null || calendarFinalMonthActualList.isEmpty())
            return;

        for (CalendarFinalMonth calendarFinalMonthActual : calendarFinalMonthActualList) {
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
                    // Отправляем сообщение в брокер сообщений, очередь с информационными сообщениями итогового календаря
                }
            } catch (RuntimeException e) {
                // Отправляем сообщение в брокер сообщений, очередь с сообщениями по ошибкам итогового календаря
                producer.sendMessage(rabbitConfig.getRoutingFinalErrorKey(),
                        String.format("Calendar months process error. Country: %s, year: %s. month: %s. Error: %s",
                                calendarFinalMonthActual.getCountry(), calendarFinalMonthActual.getYear(), calendarFinalMonthActual.getMonth(), e.getMessage()));
            }
        }
        // Отправляем сообщение в брокер сообщений, очередь с информационными сообщениями итогового календаря
        CalendarFinalMonth calendarFinalMonthActualFirst = calendarFinalMonthActualList.get(0);
        producer.sendMessage(rabbitConfig.getRoutingFinalInfoKey(),
                String.format("Calendar months process success. Country: %s, year: %s.",
                        calendarFinalMonthActualFirst.getCountry(), calendarFinalMonthActualFirst.getYear()));

    }

    private List<CalendarFinalTransition> findFinalTransitionsActual(CalendarFinalTransition calendarFinalTransition) {

        return calendarFinalTransitionsRepository.findAllByCountryAndYearAndIsArchived(
                calendarFinalTransition.getCountry(),
                calendarFinalTransition.getYear(),
                false);
    }

    // Обработка актуальных переносов по календарю
    private void processCalendarFinalTransitionList(List<CalendarFinalTransition> calendarFinalTransitionActualList) {

        if (calendarFinalTransitionActualList.isEmpty())
            return;
        CalendarFinalTransition calendarFinalTransitionFirst = calendarFinalTransitionActualList.get(0);
        try {
            // Получение списка неархивных актуальных записей по переносам из БД
            List<CalendarFinalTransition> calendarFinalTransitionDatabaseList =
                    findFinalTransitionsActual(calendarFinalTransitionFirst);

            // Цикл по актуальным записям первоисточника
            for (CalendarFinalTransition calendarFinalTransitionActual : calendarFinalTransitionActualList) {

                // Получение списка неархивных записей по полю from из БД
                List<CalendarFinalTransition> calendarFinalTransitionInDatabaseList = calendarFinalTransitionDatabaseList.stream()
                        .filter(calendarFinalTransition -> calendarFinalTransitionActual.getDayFrom().equals(calendarFinalTransition.getDayFrom()))
                        .toList();
                // Обработка полученного списка по полю from из БД
                boolean actualRecordinDataBaseExists = false;
                for (CalendarFinalTransition calendarFinalTransitionInDatabase : calendarFinalTransitionInDatabaseList) {
                    // Проверка актуальности записи из БД
                    if (calendarFinalTransitionInDatabase.getDayTo().equals(calendarFinalTransitionActual.getDayTo())) {
                        // Имеется актуальная запись в БД, обновление не требуется
                        actualRecordinDataBaseExists = true;
                    } else {
                        // Которые не соответствуют актуальной - в архив
                        calendarFinalTransitionInDatabase.setIsArchived(true);
                        calendarFinalTransitionsRepository.save(calendarFinalTransitionInDatabase);
                    }
                }
                // Если не нашли актуальную запись в БД - сохраняем из первоисточника
                if (!actualRecordinDataBaseExists) {
                    calendarFinalTransitionsRepository.save(calendarFinalTransitionActual);
                }
            }

            // Формирование списка записей из БД, которые отсутствуют в списке актуальных
            calendarFinalTransitionDatabaseList.removeIf(calendarFinalTransitionDatabase -> // Если нашли в списке актуальных - удаляем из списка
                    calendarFinalTransitionActualList.stream()
                            .anyMatch(calendarFinalTransitionActual -> (calendarFinalTransitionActual.getDayFrom().equals(calendarFinalTransitionDatabase.getDayFrom())
                                    && calendarFinalTransitionActual.getDayTo().equals(calendarFinalTransitionDatabase.getDayTo()))));
            // Отправка в архив записей из БД, которые отсутствуют в списке актуальных
            calendarFinalTransitionDatabaseList
                    .forEach(calendarFinalTransitionDatabase -> {
                        calendarFinalTransitionDatabase.setIsArchived(true);
                        calendarFinalTransitionsRepository.save(calendarFinalTransitionDatabase);
                    });
            // Отправляем сообщение в брокер сообщений, очередь с информационными сообщениями по переносам итогового календаря
            producer.sendMessage(rabbitConfig.getRoutingFinalInfoKey(),
                    String.format("Calendar transitions process success. Country: %s, year: %s.",
                            calendarFinalTransitionFirst.getCountry(), calendarFinalTransitionFirst.getYear()));
        } catch (RuntimeException e) {
            // Отправляем сообщение в брокер сообщений, очередь с сообщениями по ошибкам по переносам итогового календаря
            producer.sendMessage(rabbitConfig.getRoutingFinalErrorKey(),
                    String.format("Calendar transitions process error. Country: %s, year: %s.",
                            calendarFinalTransitionFirst.getCountry(), calendarFinalTransitionFirst.getYear()));
        }
    }

    private List<CalendarFinalStatistic> findFinalStatisticsActual(CalendarFinalStatistic calendarFinalStatistic) {

        return calendarFinalStatisticRepository.findAllByCountryAndYearAndIsArchived(
                calendarFinalStatistic.getCountry(),
                calendarFinalStatistic.getYear(),
                false);
    }

    // Обработка актуальной статистики по календарю
    private void processCalendarFinalStatistic(CalendarFinalStatistic calendarFinalStatisticActual) {

        if (calendarFinalStatisticActual == null)
            return;
        try {
            // Получение списка неархивных записей по статистике из БД
            List<CalendarFinalStatistic> calendarFinalStatisticDatabaseList =
                    findFinalStatisticsActual(calendarFinalStatisticActual);

            // Обработка полученного списка из БД
            boolean actualRecordinDataBaseExists = false;
            for (CalendarFinalStatistic calendarFinalStatisticInDatabase : calendarFinalStatisticDatabaseList) {
                // Проверка актуальности записи из БД
                if (calendarFinalStatisticInDatabase.getWorkdays().equals(calendarFinalStatisticActual.getWorkdays())
                        && calendarFinalStatisticInDatabase.getHolidays().equals(calendarFinalStatisticActual.getHolidays())) {
                    // Имеется актуальная запись в БД, обновление не требуется
                    actualRecordinDataBaseExists = true;
                } else {
                    // Которые не соответствуют актуальной - в архив
                    calendarFinalStatisticInDatabase.setIsArchived(true);
                    calendarFinalStatisticRepository.save(calendarFinalStatisticInDatabase);
                }
            }
            // Если не нашли актуальную запись в БД - сохраняем из первоисточника
            if (!actualRecordinDataBaseExists) {
                calendarFinalStatisticRepository.save(calendarFinalStatisticActual);
            }

            // Отправляем сообщение в брокер сообщений, очередь с информационными сообщениями по статистике итогового календаря
            producer.sendMessage(rabbitConfig.getRoutingFinalInfoKey(),
                    String.format("Calendar statistics process success. Country: %s, year: %s.",
                            calendarFinalStatisticActual.getCountry(), calendarFinalStatisticActual.getYear()));
        } catch (RuntimeException e) {
            // Отправляем сообщение в брокер сообщений, очередь с сообщениями по ошибкам по статистике итогового календаря
            producer.sendMessage(rabbitConfig.getRoutingFinalErrorKey(),
                    String.format("Calendar statistics process error. Country: %s, year: %s.",
                            calendarFinalStatisticActual.getCountry(), calendarFinalStatisticActual.getYear()));
        }
    }

    // Обработка данных по календарю
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
        processCalendarFinalMonthList(calendarFinalMonthList);

        // Обработка переносов
        List<CalendarFinalTransition> calendarFinalTransitionList = calendarData.getTransitions().stream()
                .map(calendarDataTransition -> new CalendarFinalTransition(
                        calendarData.getCountry(),
                        calendarData.getYear(),
                        calendarDataTransition.getFrom(),
                        calendarDataTransition.getTo(),
                        LocalDateTime.now(),
                        false))
                .toList();
        processCalendarFinalTransitionList(calendarFinalTransitionList);

        CalendarFinalStatistic calendarFinalStatistic = new CalendarFinalStatistic(
                calendarData.getCountry(),
                calendarData.getYear(),
                calendarData.getStatistic().getWorkdays(),
                calendarData.getStatistic().getHolidays(),
                LocalDateTime.now(),
                false);
        processCalendarFinalStatistic(calendarFinalStatistic);
    }

    public void processCalendarOriginal(String calendarDataString, String country) {
        // Преобразование календаря-строки в календарь-объект
        CalendarData calendarData = parseCalendarOriginal(calendarDataString);
        calendarData.setCountry(country);
        // Обработка данных календаря
        processCalendarData(calendarData);
    }
}
