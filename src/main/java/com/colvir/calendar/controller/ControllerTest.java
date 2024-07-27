package com.colvir.calendar.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class ControllerTest {

    @GetMapping("method1")
    public String method1() {
        return "Test controller method1";
    }

    @GetMapping("method2")
    public String method2() {
        return "Test controller method2";
    }
}
