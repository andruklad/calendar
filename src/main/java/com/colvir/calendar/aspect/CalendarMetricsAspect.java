package com.colvir.calendar.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@AllArgsConstructor
public class CalendarMetricsAspect {

    private final MeterRegistry meterRegistry;

    @Pointcut("within(com.colvir.calendar.controller.CalendarController)")
    public void сalendarControllerMethods() {
    }

    @Pointcut("execution(public * com.colvir.calendar.service.CalendarOriginalService.loadCalendarOriginalAll())")
    public void loadCalendarOriginalAllMethod() {
    }

    // Мониторинг вызова методов контроллера CalendarController
    @Around("сalendarControllerMethods()")
    public Object logCallCalendarControllerMethods(ProceedingJoinPoint joinPoint) {

        String methodName = joinPoint.getSignature().getName();
        Counter callCounter = meterRegistry
                .counter("calendar_controller_call_count"
                        , "method"
                        , methodName);
        callCounter.increment();
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    // Мониторинг времени загрузки календаря
    @Around("loadCalendarOriginalAllMethod()")
    public Object logTimeExecutionloadCalendarOriginalAll(ProceedingJoinPoint joinPoint) {

        long startTime = System.nanoTime();
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            long endTime = System.nanoTime();
            String metricName =  "calendar_load_execution_time";
            Timer.builder(metricName)
                    .description("Execution time")
                    .register(meterRegistry)
                    .record(endTime - startTime, TimeUnit.NANOSECONDS);
        }
    }
}
