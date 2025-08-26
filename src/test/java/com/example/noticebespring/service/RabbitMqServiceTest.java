package com.example.noticebespring.service;


import com.example.noticebespring.common.config.rabiitMQ.RabbitMqProperties;
import com.example.noticebespring.common.helper.RedisCacheHelper;
import com.example.noticebespring.common.util.RedisKeyUtil;
import com.example.noticebespring.dto.boardSubscription.postNotification.PostSummaryDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoGroupDto;
import com.example.noticebespring.dto.email.EmailPostContentDto;
import com.example.noticebespring.service.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import jakarta.mail.MessagingException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMqServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RabbitMqService rabbitMqService;

    @Test
    void 이메일_전송_실패_후_재시도_전송() throws MessagingException {
        // Given
        Map<String, java.util.List<Integer>> postTypes = Map.of(
                "학사", Arrays.asList(101)
        );

        UserSubscriptionInfoGroupDto userGroupDto = new UserSubscriptionInfoGroupDto(
                1,
                "user1@email.com",
                100,
                "자유게시판",
                postTypes,
                "202506261230"
        );

        // Message와 MessageProperties Mock 설정 (첫 번째 시도)
        Message mockMessage = mock(Message.class);
        MessageProperties mockProperties = mock(MessageProperties.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put("retryCount", 0); // 첫 번째 시도

        when(mockMessage.getMessageProperties()).thenReturn(mockProperties);
        when(mockProperties.getHeaders()).thenReturn(headers);

        // Redis 설정
        PostSummaryDto post1 = new PostSummaryDto(100, 101, "학사", "학사 공지", "내용 요약", LocalDate.now());

        // RabbitMqProperties Mock 추가
        RabbitMqProperties mockProperties2 = mock(RabbitMqProperties.class);
        RabbitMqProperties.Exchange mockExchange = mock(RabbitMqProperties.Exchange.class);
        RabbitMqProperties.Routing mockRouting = mock(RabbitMqProperties.Routing.class);
        RabbitMqProperties.Routing.Key mockKey = mock(RabbitMqProperties.Routing.Key.class);

        when(mockProperties2.getExchange()).thenReturn(mockExchange);
        when(mockExchange.getName()).thenReturn("email-exchange");
        when(mockProperties2.getRouting()).thenReturn(mockRouting);
        when(mockRouting.getKey()).thenReturn(mockKey);
        when(mockKey.getEmailRetry()).thenReturn("email-retry-key");

        // RabbitMqProperties 주입
        ReflectionTestUtils.setField(rabbitMqService, "rabbitMqProperties", mockProperties2);

        try (MockedStatic<RedisKeyUtil> mockedRedisKeyUtil = mockStatic(RedisKeyUtil.class)) {
            mockedRedisKeyUtil.when(() -> RedisKeyUtil.generatePostKey("202506261230", 100, 101))
                    .thenReturn("202506261230_100_101");

            when(redisCacheHelper.getPost("202506261230_100_101")).thenReturn(post1);

            // 이메일 전송 실패 설정
            doThrow(new MessagingException("이메일 전송 실패"))
                    .when(emailService).sendNewPostNotificationEmail(any(EmailPostContentDto.class));

            // When - 올바른 메서드 호출
            rabbitMqService.receiveUserSubscriptionInfoMessage(userGroupDto, mockMessage);

            // Then
            // 1. 이메일 서비스가 호출되었는지 확인
            verify(emailService, times(1)).sendNewPostNotificationEmail(any(EmailPostContentDto.class));

            // 2. 재시도 메시지가 전송되었는지 확인
            verify(rabbitTemplate, times(1)).convertAndSend(
                    eq("email-exchange"),
                    eq("email-retry-key"),
                    eq(userGroupDto),
                    any(org.springframework.amqp.core.MessagePostProcessor.class)
            );
        }
    }

    @Test
    void 이메일_전송_3회_실패_후_중단() throws MessagingException {
        // Given
        Map<String, java.util.List<Integer>> postTypes = Map.of(
                "학사", Arrays.asList(101)
        );

        UserSubscriptionInfoGroupDto userGroupDto = new UserSubscriptionInfoGroupDto(
                1,
                "user1@email.com",
                100,
                "자유게시판",
                postTypes,
                "202506261230"
        );

        // Message와 MessageProperties Mock 설정 (3번째 재시도)
        Message mockMessage = mock(Message.class);
        MessageProperties mockProperties = mock(MessageProperties.class);
        Map<String, Object> headers = new HashMap<>();
        headers.put("retryCount", 3); // 이미 3번 시도함

        when(mockMessage.getMessageProperties()).thenReturn(mockProperties);
        when(mockProperties.getHeaders()).thenReturn(headers);

        // Redis 설정
        PostSummaryDto post1 = new PostSummaryDto(100, 101, "학사", "학사 공지", "내용 요약", LocalDate.now());

        try (MockedStatic<RedisKeyUtil> mockedRedisKeyUtil = mockStatic(RedisKeyUtil.class)) {
            mockedRedisKeyUtil.when(() -> RedisKeyUtil.generatePostKey("202506261230", 100, 101))
                    .thenReturn("202506261230_100_101");

            when(redisCacheHelper.getPost("202506261230_100_101")).thenReturn(post1);

            // 이메일 전송 실패 설정
            doThrow(new MessagingException("이메일 전송 실패"))
                    .when(emailService).sendNewPostNotificationEmail(any(EmailPostContentDto.class));

            // When - 올바른 메서드 호출
            rabbitMqService.receiveUserSubscriptionInfoMessage(userGroupDto, mockMessage);

            // Then
            // 1. 이메일 서비스가 호출되었는지 확인
            verify(emailService, times(1)).sendNewPostNotificationEmail(any(EmailPostContentDto.class));

            // 2. 재시도 메시지가 전송되지 않았는지 확인 (3회 초과이므로)
            verify(rabbitTemplate, never()).convertAndSend(
                    anyString(),
                    anyString(),
                    any(UserSubscriptionInfoGroupDto.class),
                    any(org.springframework.amqp.core.MessagePostProcessor.class)
            );
        }
    }
}