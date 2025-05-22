package com.example.noticebespring.common.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private List<String> exposedHeaders;
    private Boolean allowCredentials;
    private Long maxAge;


    @PostConstruct
    public void logLoadedProperties() {
        log.debug("CORS 설정 로드됨:");
        log.debug("  Origins      :  {}", allowedOrigins);
        log.debug("  Methods      :  {}", allowedMethods);
        log.debug("  Headers      :  {}", allowedHeaders);
        log.debug("  ExposedHeaders:  {}", exposedHeaders);
        log.debug("  Credentials  :  {}", allowCredentials);
        log.debug("  Max Age      :  {}", maxAge);
    }
}
