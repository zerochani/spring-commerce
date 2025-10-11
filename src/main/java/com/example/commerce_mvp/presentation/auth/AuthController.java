package com.example.commerce_mvp.presentation.auth;

import com.example.commerce_mvp.application.auth.AuthService;
import com.example.commerce_mvp.application.auth.dto.AccessTokenOnlyResponseDto;
import com.example.commerce_mvp.application.auth.dto.RefreshTokenRequestDto;
import com.example.commerce_mvp.application.auth.dto.TokenRefreshResultDto;
import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenOnlyResponseDto> refreshToken(
            @RequestBody(required = false) @Valid RefreshTokenRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String refreshToken = null;

        // 1. Request Body에서 Refresh Token 확인
        if (request != null && request.getRefreshToken() != null) {
            refreshToken = request.getRefreshToken();
        }
        // 2. Cookie에서 Refresh Token 확인
        else {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refresh_token".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Refresh Token이 제공되지 않았습니다.");
        }

        // 토큰 재발급
        TokenRefreshResultDto result = authService.refreshToken(refreshToken);

        // 새로운 Refresh Token을 HttpOnly 쿠키에 설정
        Cookie refreshTokenCookie = new Cookie("refresh_token", result.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        // refreshTokenCookie.setSecure(true); // HTTPS 환경에서만 사용
        httpResponse.addCookie(refreshTokenCookie);

        // Access Token만 응답으로 반환
        AccessTokenOnlyResponseDto response = AccessTokenOnlyResponseDto.builder()
                .accessToken(result.getAccessToken())
                .grantType(result.getGrantType())
                .build();

        log.info("토큰 재발급 API 호출 완료");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody(required = false) @Valid RefreshTokenRequestDto request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        String refreshToken = null;

        // 1. Request Body에서 Refresh Token 확인
        if (request != null && request.getRefreshToken() != null) {
            refreshToken = request.getRefreshToken();
        }
        // 2. Cookie에서 Refresh Token 확인
        else {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refresh_token".equals(cookie.getName())) {
                        refreshToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // 서버 측에서 Refresh Token 무효화
        if (refreshToken != null) {
            authService.revokeRefreshToken(refreshToken);
        }

        // Refresh Token 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        httpResponse.addCookie(refreshTokenCookie);

        log.info("로그아웃 완료 - Refresh Token 무효화됨");
        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }
}
