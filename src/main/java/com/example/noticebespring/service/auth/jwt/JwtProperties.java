package com.example.noticebespring.service.auth.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private long expiration = 1296000000; // 기본값 15일
}

