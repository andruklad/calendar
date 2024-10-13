package com.colvir.calendar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class TestUtils {

    public static String getCalendarDataString() {

        String calendarDataString;
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("calendarData.json")) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readValue(in, JsonNode.class);
            calendarDataString = mapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return calendarDataString;
    }
}
