package com.example.noticebespring.service.auth.social.user;

import com.example.noticebespring.common.config.SocialConfig;
import com.example.noticebespring.entity.User;
import com.example.noticebespring.entity.SocialAccount;
import com.example.noticebespring.repository.SocialAccountRepository;
import com.example.noticebespring.repository.UserRepository;
import com.example.noticebespring.service.auth.social.SocialProviderFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public abstract class AbstractUserInfoService implements SocialUserInfoService {
    private static final Logger logger = LoggerFactory.getLogger(AbstractUserInfoService.class);
    protected final UserRepository userRepository;
    protected final SocialAccountRepository socialAccountRepository;
    protected final SocialProviderFactory providerFactory;
    protected final RestClient restClient;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    @Setter
    protected String provider;

    @Transactional
    @Override   // 액세스 토큰으로 사용자 정보 처리
    public User processUser(String accessToken) {
        logger.info("사용자 정보 처리 시작 - provider: {}", provider);
        SocialConfig.Provider config = providerFactory.getProviderConfig(provider);

        String userInfoResponse = restClient.get()
                .uri(config.getUserInfoUri())
                .header("Authorization", "Bearer " +accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        try{
            JsonNode jsonNode = objectMapper.readTree(userInfoResponse);
            String providerId = extractProviderId(jsonNode);
            String email = extractEmail(jsonNode); // 마이페이지에서 설정 가능
            logger.info("사용자 정보 추출 - provider: {}, providerId: {}, email: {} ", provider, providerId, email);

            // 1. 동일 프로바이더의 계정이 이미 존재하는지 확인 -> 동일 프로바이더로 로그인
            Optional<SocialAccount> existingSocialAccount =
            socialAccountRepository.findByProviderAndProviderId(SocialAccount.Provider.valueOf(provider.toUpperCase()), providerId);
            if(existingSocialAccount.isPresent()){
                logger.info("기존 사용자 로그인 - provider: {}, userId: {} ", provider, existingSocialAccount.get().getUser().getId());
                return existingSocialAccount.get().getUser();

            }

            //2. 신규 사용자일 경우 -> 새로운 소셜 계정 생성 및 회원가입 처리
            User user = User.builder()
                    .email(email)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);
            logger.info("신규 사용자 등록 - provider: {}, userId: {}", provider, user.getId());


            SocialAccount socialAccount = SocialAccount.builder()
                    .user(user)
                    .provider(getProviderType())
                    .providerId(providerId)
                    .build();

            socialAccountRepository.save(socialAccount);

            return user;

        } catch (Exception e) {
            logger.error("사용자 정보 처리 실패 - provider: {}, response: {}", provider, userInfoResponse, e);
            throw new RuntimeException("사용자 정보를 등록하지 못했습니다.", e);
        }
    }

    protected abstract SocialAccount.Provider getProviderType();

    protected abstract String extractProviderId(JsonNode jsonNode);

    protected abstract String extractEmail(JsonNode jsonNode);
}

