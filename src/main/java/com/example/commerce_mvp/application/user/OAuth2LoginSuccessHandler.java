package com.example.commerce_mvp.application.user;


import com.example.commerce_mvp.application.auth.AuthService;
import com.example.commerce_mvp.application.auth.dto.TokenResponseDto;
import com.example.commerce_mvp.application.auth.dto.AccessTokenOnlyResponseDto;
import com.example.commerce_mvp.application.user.dto.OAuthAttributes;
import com.example.commerce_mvp.config.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        // 각 OAuth 제공자별로 올바른 userNameAttributeName 사용
        String userNameAttributeName = getProviderUserNameAttributeName(registrationId);
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        String email = attributes.getEmail();

        //토큰 생성
        TokenResponseDto tokenInfo = jwtTokenProvider.generateToken(email, authentication.getAuthorities());
        log.info("발급된 Access Token (Subject: {}): {}", email, tokenInfo.getAccessToken());

        // Refresh Token을 DB에 저장
        authService.saveRefreshToken(tokenInfo.getRefreshToken(), email);

        //refresh token을 httponly 쿠키에 담기
        Cookie refreshTokenCookie = new Cookie("refresh_token", tokenInfo.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        //refreshTokenCookie.setSecure(true); // HTTPS 환경에서만 사용
        response.addCookie(refreshTokenCookie);

        //access token을 json 응답 본문에 담기
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        AccessTokenOnlyResponseDto accessTokenDto = AccessTokenOnlyResponseDto.builder()
                .accessToken(tokenInfo.getAccessToken())
                .grantType("Bearer")
                .build();
        String result = objectMapper.writeValueAsString(accessTokenDto);
        response.getWriter().write(result);

    }

    private String getProviderUserNameAttributeName(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> "sub";
            case "naver" -> "response";
            case "kakao" -> "id";
            default -> "sub";
        };
    }
}
