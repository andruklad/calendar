package com.colvir.calendar.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class Config {

    private final String calendarUrl;

    private final String calendarCountryList;

    private final String calendarYearList;

    public Config(@Value("${app.calendar.url}") String calendarUrl,
                  @Value("${app.calendar.countryList}") String calendarCountryList,
                  @Value("${app.calendar.yearList}") String calendarYearList) {

        this.calendarUrl = calendarUrl;
        this.calendarCountryList = calendarCountryList;
        this.calendarYearList = calendarYearList;
    }
}
