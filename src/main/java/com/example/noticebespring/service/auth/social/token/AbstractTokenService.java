package com.example.noticebespring.service.auth.social.token;

import com.example.noticebespring.common.config.SocialConfig;
import com.example.noticebespring.service.auth.social.SocialProviderFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public abstract class AbstractTokenService implements SocialTokenService {
    protected final SocialProviderFactory socialProviderFactory;
    protected final RestClient restClient;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Getter
    @Setter
    protected String provider;


    @Override       
    public String getToken(String code, String state) { // 인가 코드로 액세스 토큰 반환
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
            return jsonNode.get("access_token").asText();

        } catch (Exception e) {
            throw new RuntimeException(provider + "의 액세스 토큰을 불러오는데 실패했습니다.", e);
        }
    }
}
