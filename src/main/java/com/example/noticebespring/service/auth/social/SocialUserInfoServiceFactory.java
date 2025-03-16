package com.example.noticebespring.service.auth.social;

import com.example.noticebespring.service.auth.social.user.AbstractUserInfoService;
import com.example.noticebespring.service.auth.social.user.SocialUserInfoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor    // 프로바이더에 맞는 사용자 정보 서비스를 생성하는 팩토리
public class SocialUserInfoServiceFactory {
    private final List<SocialUserInfoService> services;
    private final Map<String, SocialUserInfoService> serviceMap = new HashMap<>();

    @PostConstruct
    public void init(){
        for(SocialUserInfoService service: services){
            if(service instanceof AbstractUserInfoService){
                String provider = ((AbstractUserInfoService) service).getProvider();
                serviceMap.put(provider, service);
            }
        }
    }

    public SocialUserInfoService getService(String provider){
        SocialUserInfoService service = serviceMap.get(provider);
        if (service == null){
            throw new IllegalArgumentException("해당 프로바이더에 대한 서비스가 존재하지 않습니다");
        }
        return service;
    }
}
