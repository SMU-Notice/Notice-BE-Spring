package com.example.noticebespring.service.auth.social;

import com.example.noticebespring.common.config.SocialConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

//프로바이더 별로 SocialConfig 제공
@Slf4j
@Component
@RequiredArgsConstructor
public class SocialProviderFactory {

    private final SocialConfig socialConfig;

    public SocialConfig.Provider getProviderConfig(String provider){
        String key = provider.toLowerCase();
        System.out.println("Requested provider: " + key);
        System.out.println("Available providers: " + socialConfig.getProviders().keySet());
        SocialConfig.Provider config = socialConfig.getProviders().get(provider.toLowerCase());
        if (config == null) {
            throw new IllegalArgumentException("해당 제공자는 지원되지 않습니다.");
        }
        log.info("Provider config loaded - provider: {}, tokenUri: {}, clientId: {}",
                provider, config.getTokenUri(), config.getClientId());
        return config;
    }

}
