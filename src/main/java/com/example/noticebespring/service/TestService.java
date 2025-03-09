package com.example.noticebespring.service;

import com.example.noticebespring.dto.TestDto;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    public TestDto getTestMessage() {
        return new TestDto("Hello, Spring Boot.");
    }
}

