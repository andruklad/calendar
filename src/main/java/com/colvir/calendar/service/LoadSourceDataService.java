package com.colvir.calendar.service;

import com.colvir.calendar.config.Config;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class LoadSourceDataService {

    private final String FILE_NAME = "calendar.json";

    private final Config config;

    public String loadFromUrl(String country, String year) throws IOException {

        String calendarUrl = String.format("%s/%s/%s/%s", config.getCalendarUrl(), country, year, FILE_NAME);
        return IOUtils.toString(URI.create(calendarUrl), StandardCharsets.UTF_8);
    }
}
