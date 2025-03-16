package com.example.noticebespring.service.auth.social;

import com.example.noticebespring.service.auth.social.token.AbstractTokenService;
import com.example.noticebespring.service.auth.social.token.SocialTokenService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor    // 프로바이더에 맞는 토큰 서비스를 생성하는 팩토리
public class SocialTokenServiceFactory {
    private final List<SocialTokenService> services;
    private final Map<String, SocialTokenService> serviceMap = new HashMap<>();

    @PostConstruct
    public void init(){
        for(SocialTokenService service: services){
            if(service instanceof AbstractTokenService){
                String provider = ((AbstractTokenService) service).getProvider();
                serviceMap.put(provider, service);
            }
        }
    }

    public SocialTokenService getService(String provider){
        SocialTokenService service = serviceMap.get(provider);
        if (service == null){
            throw new IllegalArgumentException("해당 프로바이더에 대한 서비스가 존재하지 않습니다");
        }
        return service;
    }
}

