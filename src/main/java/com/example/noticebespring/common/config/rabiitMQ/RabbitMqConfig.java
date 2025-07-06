package com.example.noticebespring.common.config.rabiitMQ;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class RabbitMqConfig {

    private final RabbitMqProperties rabbitMqProperties;

    // org.springframework.amqp.core.Queue
    // 각각의 큐 등록
    @Bean
    public Queue testQueue() {
        return new Queue(rabbitMqProperties.getQueue().getTest());
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(rabbitMqProperties.getQueue().getEmail());
    }

    @Bean
    public Queue emailRetryQueue() {
        return QueueBuilder.durable(rabbitMqProperties.getQueue().getEmailRetry())
                .withArgument("x-message-ttl", 30000) // 30초 대기
                .withArgument("x-dead-letter-exchange", rabbitMqProperties.getExchange().getName()) // 원래 메일 처리 큐로
                .withArgument("x-dead-letter-routing-key", rabbitMqProperties.getRouting().getKey().getEmail())
                .build();
    }

    /**
     * 지정된 Exchange 이름으로 Direct Exchange Bean 을 생성
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(rabbitMqProperties.getExchange().getName());
    }

    /**
     * 주어진 Queue 와 Exchange 을 Binding 하고 Routing Key 을 이용하여 Binding Bean 생성
     **/
    // 각 큐를 Exchange에 바인딩 (Routing Key 별로)
    @Bean
    public Binding testBinding() {
        return BindingBuilder.bind(testQueue()).to(directExchange()).with(rabbitMqProperties.getRouting().getKey().getTest());
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(directExchange()).with(rabbitMqProperties.getRouting().getKey().getEmail());
    }

    @Bean
    public Binding emailRetryBinding() {
        return BindingBuilder.bind(emailRetryQueue()).to(directExchange()).with(rabbitMqProperties.getRouting().getKey().getEmailRetry());
    }

    /**
     * RabbitMQ 연동을 위한 ConnectionFactory 빈을 생성하여 반환
     **/
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitMqProperties.getHost());
        connectionFactory.setPort(rabbitMqProperties.getPort());
        connectionFactory.setUsername(rabbitMqProperties.getUsername());
        connectionFactory.setPassword(rabbitMqProperties.getPassword());
        return connectionFactory;
    }

    /**
     * RabbitTemplate
     * ConnectionFactory 로 연결 후 실제 작업을 위한 Template
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * 직렬화(메세지를 JSON 으로 변환하는 Message Converter)
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}