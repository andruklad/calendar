package com.colvir.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransitionResponse {

    private String dayFrom;

    private String dayTo;
}
