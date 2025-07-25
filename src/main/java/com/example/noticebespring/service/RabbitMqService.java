package com.example.noticebespring.service;

import com.example.noticebespring.common.config.rabiitMQ.RabbitMqProperties;
import com.example.noticebespring.common.helper.RedisCacheHelper;
import com.example.noticebespring.common.util.RedisKeyUtil;
import com.example.noticebespring.dto.boardSubscription.postNotification.PostSummaryDto;
import com.example.noticebespring.dto.boardSubscription.postNotification.UserSubscriptionInfoGroupDto;
import com.example.noticebespring.dto.boardSubscription.rabbitMQ.TestMessageDto;
import com.example.noticebespring.dto.email.EmailPostContentDto;
import com.example.noticebespring.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.MessagingException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Queue 로 메세지를 발핼한 때에는 RabbitTemplate 의 ConvertAndSend 메소드를 사용하고
 * Queue 에서 메세지를 구독할때는 @RabbitListener 을 사용
 *
 **/
@Slf4j
@RequiredArgsConstructor
@Service
public class RabbitMqService {

    // @Value 어노테이션들 모두 제거하고 Properties 사용
    private final RabbitMqProperties rabbitMqProperties;
    private final RabbitTemplate rabbitTemplate;
    private final RedisCacheHelper redisCacheHelper;
    private final EmailService emailService;

    /**
     * 1. Queue 로 메세지를 발행
     * 2. Producer 역할 -> Direct Exchange 전략
     **/
    public void sendTestMessage(TestMessageDto messageDto) {
        log.info("messagge send: {}",messageDto.toString());
        this.rabbitTemplate.convertAndSend(
                rabbitMqProperties.getExchange().getName(),
                rabbitMqProperties.getRouting().getKey().getTest(),
                messageDto
        );
    }

    public void sendEmailMessage(UserSubscriptionInfoGroupDto userSubscriptionInfoGroupDto) {
        log.info("messagge send: {}",userSubscriptionInfoGroupDto.toString());
        this.rabbitTemplate.convertAndSend(
                rabbitMqProperties.getExchange().getName(),
                rabbitMqProperties.getRouting().getKey().getEmail(),
                userSubscriptionInfoGroupDto
        );
    }

    // 재시도 전송
    public void sendRetryEmailMessage(UserSubscriptionInfoGroupDto userSubscriptionInfoGroupDto, int retryCount) {
        log.warn("Retrying email send. Attempt: {}, Target: {}", retryCount, userSubscriptionInfoGroupDto.email());

        MessagePostProcessor processor = msg -> {
            msg.getMessageProperties().setHeader("retryCount", retryCount);
            return msg;
        };

        rabbitTemplate.convertAndSend(
                rabbitMqProperties.getExchange().getName(),
                rabbitMqProperties.getRouting().getKey().getEmailRetry(),
                userSubscriptionInfoGroupDto,
                processor
        );
    }

    /**
     * 1. Queue 에서 메세지를 구독
     **/
    @RabbitListener(queues = "${app.rabbitmq.queue.test}")
    public void receiveTestMessage(TestMessageDto messageDto) {
        log.info("Received Message : {}",messageDto.toString());
    }

    /**
     * 1. Queue 에서 메세지를 구독
     **/
    @RabbitListener(queues = "${app.rabbitmq.queue.email}", concurrency = "3-5")
    public void receiveUserSubscriptionInfoMessage(UserSubscriptionInfoGroupDto userSubscriptionInfoGroupDto, Message message) {
        log.info("Received Message : {}",userSubscriptionInfoGroupDto.toString());

        // 재전송 개수 설정
        Integer retryCount = (Integer) message.getMessageProperties()
                .getHeaders()
                .getOrDefault("retryCount", 0);

        // 메일을 실을 내용을 담을꺼 생성
        List<PostSummaryDto> postSummaries = new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry : userSubscriptionInfoGroupDto.postTypes().entrySet()) {
            String postType = entry.getKey();
            List<Integer> postIds = entry.getValue();

            for (Integer postId : postIds) {
                String key = RedisKeyUtil.generatePostKey(
                        userSubscriptionInfoGroupDto.timestamp(),
                        userSubscriptionInfoGroupDto.boardId(),
                        postId
                );

                PostSummaryDto postSummaryDto = redisCacheHelper.getPost(key);
                postSummaries.add(postSummaryDto);
            }
        }

        EmailPostContentDto emailDto = new EmailPostContentDto(
                userSubscriptionInfoGroupDto.email(),
                userSubscriptionInfoGroupDto.boardName(),
                userSubscriptionInfoGroupDto.campus(),
                postSummaries
        );

        log.info("Sending email to: {}", emailDto.email());
        // 이메일 전송
        try {
            log.info("Sending email to: {}", emailDto.email());
            emailService.sendNewPostNotificationEmail(emailDto);
        } catch (MessagingException e) {
            if (retryCount >= 3) {
                log.error("메일 전송 3회 이상 실패 - 중단: {}", emailDto.email(), e);
            } else {
                // retryCount 증가시키고 DLQ로 보내기 (TTL 이후 재투입됨)
                this.sendRetryEmailMessage(userSubscriptionInfoGroupDto, retryCount);
            }
        }
    }
}