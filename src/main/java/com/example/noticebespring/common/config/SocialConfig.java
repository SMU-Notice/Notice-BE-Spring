package com.example.noticebespring.common.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "social")
@Data
public class SocialConfig {
    private Map<String, Provider> providers = new HashMap<>();

    @PostConstruct
    public void init() {
        System.out.println("SocialConfig providers: " + providers);
        for (Map.Entry<String, Provider> entry : providers.entrySet()) {
            System.out.println("Provider: " + entry.getKey());
            System.out.println("  clientId: " + entry.getValue().getClientId());
            System.out.println("  clientSecret: " + entry.getValue().getClientSecret());
            System.out.println("  redirectUri: " + entry.getValue().getRedirectUri());
            System.out.println("  tokenUrl: " + entry.getValue().getTokenUri());
            System.out.println("  userInfoUrl: " + entry.getValue().getUserInfoUri());
        }
    }

    @Data
    public static class Provider {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUri;
        private String userInfoUri;
    }

}
