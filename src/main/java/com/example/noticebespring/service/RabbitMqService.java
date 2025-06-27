package com.example.noticebespring.service;

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
import org.springframework.beans.factory.annotation.Value;
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


    @Value("${rabbitmq.queue.test}")
    private String testQueueName;

    @Value("${rabbitmq.queue.email}")
    private String emailQueueName;

    @Value("${rabbitmq.queue.email-retry}")
    private String emailRetryQueueName;

    @Value("${rabbitmq.exchange.name}")
    private String emailExchangeName;

    @Value("${rabbitmq.routing.key.test}")
    private String testRoutingKey;

    @Value("${rabbitmq.routing.key.email}")
    private String emailRoutingKey;

    @Value("${rabbitmq.routing.key.email-retry}")
    private String emailRetryRoutingKey;

    private final RabbitTemplate rabbitTemplate;
    private final RedisCacheHelper redisCacheHelper;
    private final EmailService emailService;

    /**
     * 1. Queue 로 메세지를 발행
     * 2. Producer 역할 -> Direct Exchange 전략
     **/
    public void sendTestMessage(TestMessageDto messageDto) {
        log.info("messagge send: {}",messageDto.toString());
        this.rabbitTemplate.convertAndSend(emailExchangeName, testRoutingKey, messageDto);
    }

    public void sendEmailMessage(UserSubscriptionInfoGroupDto userSubscriptionInfoGroupDto) {
        log.info("messagge send: {}",userSubscriptionInfoGroupDto.toString());
        this.rabbitTemplate.convertAndSend(emailExchangeName, emailRoutingKey, userSubscriptionInfoGroupDto);
    }

    // 재시도 전송
    public void sendRetryEmailMessage(UserSubscriptionInfoGroupDto userSubscriptionInfoGroupDto, int retryCount) {
        log.warn("Retrying email send. Attempt: {}, Target: {}", retryCount, userSubscriptionInfoGroupDto.email());

        MessagePostProcessor processor = msg -> {
            msg.getMessageProperties().setHeader("retryCount", retryCount);
            return msg;
        };

        rabbitTemplate.convertAndSend(emailExchangeName, emailRetryRoutingKey, userSubscriptionInfoGroupDto, processor);
    }

    /**
     * 1. Queue 에서 메세지를 구독
     **/
    @RabbitListener(queues = "${rabbitmq.queue.test}")
    public void receiveTestMessage(TestMessageDto messageDto) {
        log.info("Received Message : {}",messageDto.toString());
    }

    /**
     * 1. Queue 에서 메세지를 구독
     **/
    @RabbitListener(queues = "${rabbitmq.queue.email}", concurrency = "3-7")
    public void receiveTestMessage(UserSubscriptionInfoGroupDto userSubscriptionInfoGroupDto, Message message) {
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
                postSummaries
        );

        // 이메일 전송
        try {
            emailService.sendNewPostNotificationEmail(emailDto);
        } catch (MessagingException e) {
            if (retryCount >= 3) {
                log.error("메일 전송 3회 이상 실패 - 중단: {}", emailDto.email(), e);
                // 여기서 DB 저장 또는 알림 로직 추가 가능
            } else {
                // retryCount 증가시키고 DLQ로 보내기 (TTL 이후 재투입됨)
                this.sendRetryEmailMessage(userSubscriptionInfoGroupDto, retryCount);
            }
        }


    }
}
