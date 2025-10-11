package com.example.commerce_mvp.application.auth;

import com.example.commerce_mvp.application.auth.dto.AccessTokenOnlyResponseDto;
import com.example.commerce_mvp.application.auth.dto.TokenResponseDto;
import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import com.example.commerce_mvp.application.user.UserService;
import com.example.commerce_mvp.config.JwtTokenProvider;
import com.example.commerce_mvp.domain.auth.RefreshToken;
import com.example.commerce_mvp.domain.auth.RefreshTokenRepository;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponseDto refreshToken(String refreshToken) {
        // DB에서 Refresh Token 조회 및 검증
        RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "유효하지 않은 Refresh Token입니다."));

        // Refresh Token 만료 확인
        if (storedRefreshToken.isExpired()) {
            refreshTokenRepository.deleteByToken(refreshToken);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN, "만료된 Refresh Token입니다.");
        }

        // JWT 토큰 유효성 검증
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            refreshTokenRepository.deleteByToken(refreshToken);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 사용자 정보 조회
        User user = userRepository.findByEmail(storedRefreshToken.getUserEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + storedRefreshToken.getUserEmail()));

        // 사용자 권한 정보 생성
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getValue());
        
        // 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(storedRefreshToken.getUserEmail(), Collections.singletonList(authority));
        
        // 새로운 Refresh Token 생성
        String newRefreshToken = jwtTokenProvider.generateRefreshToken();
        
        // 기존 Refresh Token 삭제
        refreshTokenRepository.deleteByToken(refreshToken);
        
        // 새로운 Refresh Token 저장
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(1209600); // 14일
        RefreshToken newStoredRefreshToken = RefreshToken.builder()
                .token(newRefreshToken)
                .userEmail(storedRefreshToken.getUserEmail())
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(newStoredRefreshToken);

        log.info("토큰 재발급 완료 - 사용자: {}", storedRefreshToken.getUserEmail());

        return TokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .grantType("Bearer")
                .build();
    }

    public void saveRefreshToken(String refreshToken, String userEmail) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(1209600); // 14일
        RefreshToken storedRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .userEmail(userEmail)
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(storedRefreshToken);
    }

    public void revokeRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    public void revokeAllUserTokens(String userEmail) {
        refreshTokenRepository.deleteByUserEmail(userEmail);
    }
}
