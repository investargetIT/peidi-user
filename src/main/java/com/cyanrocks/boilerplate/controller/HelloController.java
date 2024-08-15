package com.cyanrocks.boilerplate.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Api(tags = {"测试"})
public class HelloController {

    @GetMapping("/")
    @ApiOperation(value = "测试")
    public String index() {
        return "Greetings from Spring Boot!";
    }

}