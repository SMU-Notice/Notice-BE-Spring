package com.example.noticebespring.service.auth.social.token;

import com.example.noticebespring.common.config.SocialConfig;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public abstract class AbstractTokenService implements SocialTokenService {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTokenService.class);
    protected final SocialProviderFactory socialProviderFactory;
    protected final RestClient restClient;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    @Setter
    protected String provider;

    @Override       
    public String getToken(String code, String state) { // 인가 코드로 액세스 토큰 반환
        logger.debug("액세스 토큰 요청 시작 - provider: {}", provider);
        SocialConfig.Provider config = socialProviderFactory.getProviderConfig(provider);
        String tokenUri = config.getTokenUri();

        // URI 검증
        if (tokenUri == null || tokenUri.isEmpty()) {
            logger.error("토큰 URI 누락 - provider: {}", provider);
            throw new IllegalArgumentException(provider + "의 토큰 URI가 설정되지 않았습니다.");
        }
        if (!tokenUri.startsWith("http://") && !tokenUri.startsWith("https://")) {
            logger.error("잘못된 토큰 URI 형식 - provider: {}, tokenUri: {}", provider, tokenUri);
            throw new IllegalArgumentException(provider + "의 토큰 URI에 스키마(http:// 또는 https://)가 누락되었습니다.");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", config.getRedirectUri());

        if (provider == "naver" && state != null && !state.isEmpty()) {
            params.add("state", state);
        }


        String responseBody = restClient.post()
                .uri(config.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(params)
                .retrieve()
                .body(String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String accessToken = jsonNode.get("access_token").asText();
            logger.info("액세스 토큰 발급 성공 - provider: {}", provider);
            return accessToken;

        } catch (Exception e) {
            logger.error("액세스 토큰 파싱 실패 - provider: {}", provider);
            throw new RuntimeException(provider + "의 액세스 토큰을 불러오는데 실패했습니다.");
        }
    }
}
