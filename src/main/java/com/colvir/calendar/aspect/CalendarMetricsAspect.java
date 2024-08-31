package com.colvir.calendar.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
public class CalendarMetricsAspect {

    private final MeterRegistry meterRegistry;

    @Pointcut("within(com.colvir.calendar.controller.CalendarController)")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
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
}
