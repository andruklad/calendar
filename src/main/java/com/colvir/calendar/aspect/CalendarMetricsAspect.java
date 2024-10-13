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
    public void calendarControllerMethods() {
    }

    @Pointcut("within(com.colvir.calendar.controller.ExceptionHandlerController)")
    public void calendarExceptionMethods() {
    }

    @Pointcut("execution(public * com.colvir.calendar.service.CalendarOriginalService.loadCalendarOriginalAll())")
    public void loadCalendarOriginalAllMethod() {
    }

    // Мониторинг вызова методов контроллера CalendarController
    @Around("calendarControllerMethods()")
    public Object logCallCalendarControllerMethods(ProceedingJoinPoint joinPoint) {

        String metricName = "calendar_controller_call_count";
        return logCallMethods(joinPoint, metricName);
    }

    // Мониторинг генерации внутренних исключений приложения
    @Around("calendarExceptionMethods()")
    public Object logCalendarExceptionMethods(ProceedingJoinPoint joinPoint) {

        String metricName = "calendar_internal_exception_count";
        return logCallMethods(joinPoint, metricName);
    }

    private Object logCallMethods(ProceedingJoinPoint joinPoint, String metricName) {
        String methodName = joinPoint.getSignature().getName();
        Counter callCounter = meterRegistry
                .counter(metricName
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
