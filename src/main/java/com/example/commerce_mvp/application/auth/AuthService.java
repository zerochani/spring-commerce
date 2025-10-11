package com.example.commerce_mvp.application.auth;

import com.example.commerce_mvp.application.auth.dto.TokenResponseDto;
import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import com.example.commerce_mvp.application.user.UserService;
import com.example.commerce_mvp.config.JwtTokenProvider;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public TokenResponseDto refreshToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Refresh Token에서 사용자 정보 추출
        String email = jwtTokenProvider.getSubjectFromRefreshToken(refreshToken);
        
        // 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + email));

        // 사용자 권한 정보 생성
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getValue());
        
        // 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(email, Collections.singletonList(authority));
        
        // 새로운 Refresh Token 생성 (보안을 위해 Refresh Token도 갱신)
        String newRefreshToken = jwtTokenProvider.generateRefreshToken();

        log.info("토큰 재발급 완료 - 사용자: {}", email);

        return TokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .grantType("Bearer")
                .build();
    }
}
