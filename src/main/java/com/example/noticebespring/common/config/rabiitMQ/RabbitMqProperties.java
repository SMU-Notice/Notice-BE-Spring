package com.example.noticebespring.common.config.rabiitMQ;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
@AllArgsConstructor
@Getter
public class RabbitMqProperties {
    private String host;
    private int port;
    private String username;
    private String password;
    private Queue queue;
    private Exchange exchange;
    private Routing routing;

    @Getter
    @AllArgsConstructor
    public static class Queue {
        private String test;
        private String email;
        private String emailRetry; // email-retry -> emailRetry (camelCase)
    }

    @Getter
    @AllArgsConstructor
    public static class Exchange {
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public static class Routing {
        private Key key;

        @Getter
        @AllArgsConstructor
        public static class Key {
            private String test;
            private String email;
            private String emailRetry;
        }
    }
}