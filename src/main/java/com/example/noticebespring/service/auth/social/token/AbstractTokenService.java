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
        logger.info("액세스 토큰 요청 시작 - provider: {}, code: {}", provider, code);
        SocialConfig.Provider config = socialProviderFactory.getProviderConfig(provider);
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
            logger.debug("액세스 토큰 발급 성공 - provider: {}, token: {}", provider, accessToken);
            return accessToken;

        } catch (Exception e) {
            logger.error("액세스 토큰 파싱 실패 - provider: {}, response: {}", provider, responseBody, e);
            throw new RuntimeException(provider + "의 액세스 토큰을 불러오는데 실패했습니다.", e);
        }
    }
}
