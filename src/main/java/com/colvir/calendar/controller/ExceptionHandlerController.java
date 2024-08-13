package com.colvir.calendar.controller;

import com.colvir.calendar.dto.ErrorResponse;
import com.colvir.calendar.exception.MonthDataNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.colvir.calendar.model.InternalErrorStatus.MONTH_DATA_NOT_FOUND;

@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(MonthDataNotFoundException.class)
    public ResponseEntity<ErrorResponse> monthDataNotFoundException(Exception e) {

        ErrorResponse errorResponse = new ErrorResponse(MONTH_DATA_NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
