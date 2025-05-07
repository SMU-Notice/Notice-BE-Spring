package com.example.noticebespring.common.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(SocialConfig.class)
@ConfigurationProperties(prefix = "social")
@Data
@Slf4j
public class SocialConfig {
    private Map<String, Provider> providers = new HashMap<>();

    @PostConstruct
    public void init() {
        if (providers.isEmpty()) {
            log.warn("SocialConfig: No providers loaded. Check environment variables or YAML configuration.");
        } else {
            log.info("SocialConfig loaded providers:");
            for (Map.Entry<String, Provider> entry : providers.entrySet()) {
                log.info("Provider: {}", entry.getKey());
                log.info("  clientId: {}", entry.getValue().getClientId());
                log.info("  clientSecret: {}", entry.getValue().getClientSecret());
                log.info("  redirectUri: {}", entry.getValue().getRedirectUri());
                log.info("  tokenUri: {}", entry.getValue().getTokenUri());
                log.info("  userInfoUri: {}", entry.getValue().getUserInfoUri());
            }
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
