package com.example.noticebespring.controller;

import com.example.noticebespring.common.response.ApiResponse;
import com.example.noticebespring.dto.TestDto;
import com.example.noticebespring.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/test001")
    public ApiResponse<TestDto> getTestMessage() {
        TestDto response = testService.getTestMessage();
        log.info("Returning response: {}", response.message());
        return ApiResponse.ok(response);
    }
}
