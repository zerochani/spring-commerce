package com.example.commerce_mvp.application.user.dto;

import com.example.commerce_mvp.domain.user.SocialProvider;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String username;
    private final String email;
    private final SocialProvider provider;
    private final String providerId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String username, String email, SocialProvider provider, String providerId){
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.username = username;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes){
        if("google".equalsIgnoreCase(registrationId)){
            return ofGoogle(userNameAttributeName, attributes);
        }
        if("naver".equalsIgnoreCase(registrationId)){
            return ofNaver(userNameAttributeName, attributes);
        }
        if("kakao".equalsIgnoreCase(registrationId)){
            return ofKakao(userNameAttributeName, attributes);
        }
        throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + registrationId);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes){
        String username = (String) attributes.get("name");
        String email = (String) attributes.get("email");
        String providerId = (String) attributes.get(userNameAttributeName);
        
        if (username == null || email == null || providerId == null) {
            throw new IllegalArgumentException("Google OAuth에서 필수 정보가 누락되었습니다.");
        }
        
        return OAuthAttributes.builder()
                .username(username)
                .email(email)
                .provider(SocialProvider.GOOGLE)
                .providerId(providerId)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes){
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            throw new IllegalArgumentException("Naver OAuth 응답에서 response 정보가 없습니다.");
        }
        
        String username = (String) response.get("name");
        String email = (String) response.get("email");
        String providerId = (String) response.get("id");
        
        if (username == null || email == null || providerId == null) {
            throw new IllegalArgumentException("Naver OAuth에서 필수 정보가 누락되었습니다.");
        }
        
        return OAuthAttributes.builder()
                .username(username)
                .email(email)
                .provider(SocialProvider.NAVER)
                .providerId(providerId)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes){
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            throw new IllegalArgumentException("Kakao OAuth 응답에서 kakao_account 정보가 없습니다.");
        }
        
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
        if (kakaoProfile == null) {
            throw new IllegalArgumentException("Kakao OAuth 응답에서 profile 정보가 없습니다.");
        }

        String username = (String) kakaoProfile.get("nickname");
        String email = (String) kakaoAccount.get("email");
        String providerId = String.valueOf(attributes.get("id"));
        
        if (username == null || email == null || providerId == null || "null".equals(providerId)) {
            throw new IllegalArgumentException("Kakao OAuth에서 필수 정보가 누락되었습니다.");
        }

        return OAuthAttributes.builder()
                .username(username)
                .email(email)
                .provider(SocialProvider.KAKAO)
                .providerId(providerId)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }


    public User toEntity(){
        return User.builder()
                .username(username)
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .role(UserRole.USER)
                .build();
    }
}
