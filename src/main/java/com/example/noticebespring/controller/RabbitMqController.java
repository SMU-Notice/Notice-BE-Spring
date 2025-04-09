package com.example.noticebespring.controller;

import com.example.noticebespring.dto.boardSubscription.rabbitMQ.TestMessageDto;
import com.example.noticebespring.service.RabbitMqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/rabbit-mq")
public class RabbitMqController {
    private final RabbitMqService rabbitMqService;

    @PostMapping("/send/message")
    public ResponseEntity<String> sendMessage (@RequestBody TestMessageDto messageDto
    ) {
        this.rabbitMqService.sendTestMessage(messageDto);
        return ResponseEntity.ok("Message sent to RabbitMQ");
    }
}
