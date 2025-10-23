package com.example.commerce_mvp.application.auth;

import com.example.commerce_mvp.application.auth.dto.TokenResponseDto;
import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import com.example.commerce_mvp.config.JwtTokenProvider;
import com.example.commerce_mvp.domain.auth.RefreshToken;
import com.example.commerce_mvp.domain.auth.RefreshTokenRepository;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRepository;
import com.example.commerce_mvp.domain.user.UserRole;
import com.example.commerce_mvp.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("refreshToken - 유효한 토큰이면 Access/Refresh 토큰을 재발급한다")
    void refreshToken_success() {
        String oldRefreshToken = "old-token";
        String newRefreshToken = "new-token";
        String newAccessToken = "new-access-token";
        User user = TestFixtures.createUser("user@example.com", UserRole.USER);

        RefreshToken storedRefreshToken = RefreshToken.builder()
                .token(oldRefreshToken)
                .userEmail(user.getEmail())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken(oldRefreshToken)).thenReturn(Optional.of(storedRefreshToken));
        when(jwtTokenProvider.validateRefreshToken(oldRefreshToken)).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(eq(user.getEmail()), any())).thenReturn(newAccessToken);
        when(jwtTokenProvider.generateRefreshToken()).thenReturn(newRefreshToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TokenResponseDto response = authService.refreshToken(oldRefreshToken);

        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(response.getGrantType()).isEqualTo("Bearer");

        verify(refreshTokenRepository).deleteByToken(oldRefreshToken);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();
        assertThat(savedRefreshToken.getToken()).isEqualTo(newRefreshToken);
        assertThat(savedRefreshToken.getUserEmail()).isEqualTo(user.getEmail());
        assertThat(savedRefreshToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("refreshToken - 만료된 토큰이면 예외가 발생하고 기존 토큰을 삭제한다")
    void refreshToken_expiredToken() {
        String refreshToken = "expired-token";
        RefreshToken storedRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .userEmail("user@example.com")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedRefreshToken));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.refreshToken(refreshToken));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_TOKEN);
        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verifyNoInteractions(jwtTokenProvider, userRepository);
    }

    @Test
    @DisplayName("refreshToken - JWT 검증에 실패하면 예외가 발생하고 기존 토큰을 삭제한다")
    void refreshToken_invalidJwt() {
        String refreshToken = "invalid-token";
        RefreshToken storedRefreshToken = RefreshToken.builder()
                .token(refreshToken)
                .userEmail("user@example.com")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedRefreshToken));
        when(jwtTokenProvider.validateRefreshToken(refreshToken)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.refreshToken(refreshToken));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("saveRefreshToken - Refresh 토큰을 저장한다")
    void saveRefreshToken() {
        String refreshToken = "refresh-token";
        String userEmail = "user@example.com";

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.saveRefreshToken(refreshToken, userEmail);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();
        assertThat(savedToken.getToken()).isEqualTo(refreshToken);
        assertThat(savedToken.getUserEmail()).isEqualTo(userEmail);
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("revokeRefreshToken - 특정 Refresh 토큰을 제거한다")
    void revokeRefreshToken() {
        String refreshToken = "refresh-token";

        authService.revokeRefreshToken(refreshToken);

        verify(refreshTokenRepository).deleteByToken(refreshToken);
    }

    @Test
    @DisplayName("revokeAllUserTokens - 사용자에 대한 Refresh 토큰을 모두 제거한다")
    void revokeAllUserTokens() {
        String userEmail = "user@example.com";

        authService.revokeAllUserTokens(userEmail);

        verify(refreshTokenRepository).deleteByUserEmail(userEmail);
    }
}
