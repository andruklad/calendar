package com.colvir.calendar.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class Config {

    private final String calendarUrl;

    public Config(@Value("${app.calendar.url}") String calendarUrl) {
        this.calendarUrl = calendarUrl;
    }
}
