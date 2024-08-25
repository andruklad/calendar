package com.colvir.calendar.service;

import com.colvir.calendar.dto.CalendarData;
import com.colvir.calendar.model.CalendarFinalMonth;
import com.colvir.calendar.model.CalendarFinalTransition;
import com.colvir.calendar.rabbitmq.Producer;
import com.colvir.calendar.repository.CalendarFinalMonthsRepository;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CalendarFinalMonthsRepository calendarFinalMonthsRepository;

    private final CalendarFinalTransitionsRepository calendarFinalTransitionsRepository;

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

    // TODO: 25.08.2024 Изменить сигнатуру, объект здесь не нужен
    private List<CalendarFinalTransition> findFinalTransitionsActual(CalendarFinalTransition calendarFinalTransitionActual) {
        return calendarFinalTransitionsRepository.findAllByCountryAndYearAndIsArchived(
                calendarFinalTransitionActual.getCountry(),
                calendarFinalTransitionActual.getYear(),
                false
        );
    }

    // Обработка актуальных переносов по календарю
    private void processCalendarFinalTransitionList(List<CalendarFinalTransition> calendarFinalTransitionActualList) {

        try {
            if (calendarFinalTransitionActualList.isEmpty())
                return;
            // Получение списка неархивных актуальных записей по переносам из БД
            List<CalendarFinalTransition> calendarFinalTransitionDatabaseList = findFinalTransitionsActual(calendarFinalTransitionActualList.get(0));

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
                // Отправляем сообщение в брокер сообщений, очередь с информационными сообщениями по переносам итогового календаря
                // TODO: 25.08.2024 Доработать отправку в брокер
            }

            // Формирование списка записей из БД, которые отсутствуют в списке актуальных
            calendarFinalTransitionDatabaseList.removeIf(calendarFinalTransitionDatabase -> // Если в списке актуальных не нашли - оставляем в списке (для удаления)
                    calendarFinalTransitionActualList.stream()
                            .filter(calendarFinalTransitionActual -> (calendarFinalTransitionActual.getDayFrom().equals(calendarFinalTransitionDatabase.getDayFrom())
                                    && calendarFinalTransitionActual.getDayTo().equals(calendarFinalTransitionDatabase.getDayTo())))
                            .findFirst().isPresent());
            // Отправка в архив записей из БД, которые отсутствуют в списке актуальных
            calendarFinalTransitionDatabaseList
                    .forEach(calendarFinalTransitionDatabase -> {
                        calendarFinalTransitionDatabase.setIsArchived(true);
                        calendarFinalTransitionsRepository.save(calendarFinalTransitionDatabase);
                    });
        } catch (RuntimeException e) {
            // Отправляем сообщение в брокер сообщений, очередь с сообщениями по ошибкам по переносам итогового календаря
            // TODO: 25.08.2024 Доработать отправку в брокер
//            producer.sendMessage(producer.getRabbitConfig().getRoutingFinalTransitionsErrorKey(),
//                    String.format("Calendar Transition load error. Country: %s, year: %s. Transition: %s. Error: %s",
//                            calendarFinalTransitionActual.getCountry(), calendarFinalTransitionActual.getYear(), calendarFinalTransitionActual.getTransition(), e.getMessage()));
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

        // TODO: 25.08.2024 Обработка статистики
    }

    public void processCalendarOriginal(String calendarDataString, String country) {
        // Преобразование календаря-строки в календарь-объект
        CalendarData calendarData = parseCalendarOriginal(calendarDataString);
        calendarData.setCountry(country);
        // Обработка данных календаря
        processCalendarData(calendarData);
    }
}
