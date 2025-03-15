package com.example.noticebespring.service.auth.social;

import com.example.noticebespring.service.auth.social.user.AbstractUserService;
import com.example.noticebespring.service.auth.social.user.SocialUserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor    // 프로바이더에 맞는 사용자 정보 서비스를 생성하는 팩토리
public class SocialUserServiceFactory {
    private final List<SocialUserService> services;
    private final Map<String, SocialUserService> serviceMap = new HashMap<>();

    @PostConstruct
    public void init(){
        for(SocialUserService service: services){
            if(service instanceof AbstractUserService){
                String provider = ((AbstractUserService) service).getProvider();
                serviceMap.put(provider, service);
            }
        }
    }

    public SocialUserService getService(String provider){
        SocialUserService service = serviceMap.get(provider);
        if (service == null){
            throw new IllegalArgumentException("해당 프로바이더에 대한 서비스가 존재하지 않습니다");
        }
        return service;
    }
}
