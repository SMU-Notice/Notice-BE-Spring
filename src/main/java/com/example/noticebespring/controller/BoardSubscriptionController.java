package com.example.noticebespring.controller;


import com.example.noticebespring.dto.boardSubscription.SubscriptionEmailRequestDto;
import com.example.noticebespring.dto.boardSubscription.SubscriptionRequestDto;
import com.example.noticebespring.dto.boardSubscription.SubscriptionResponseDto;
import com.example.noticebespring.service.BoardSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/board-subscription")
@RestController
public class BoardSubscriptionController {

    private final BoardSubscriptionService boardSubscriptionService;

    @GetMapping("")
    public SubscriptionResponseDto getSubscriptions() {
        return boardSubscriptionService.getSubscriptions();
    }

    @PostMapping("")
    public SubscriptionResponseDto manageSubscriptions(@RequestBody SubscriptionRequestDto subscriptionRequestDto) {
        return boardSubscriptionService.manageSubscriptions(subscriptionRequestDto);
    }

    @PostMapping("/send-new-posts")
    public void sendNewPostsEmail(@RequestBody SubscriptionEmailRequestDto subscriptionEmailRequestDto) {
        log.info("New posts received for subscription: {}", subscriptionEmailRequestDto);

        // 이메일 전송을 위한 처리
        emailService.sendNewPostNotification(subscriptionEmailRequestDto.newPosts());

        log.info("New posts subscription email sent successfully.");
    }
}
