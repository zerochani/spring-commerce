package com.example.commerce_mvp.config;



import com.example.commerce_mvp.application.auth.dto.TokenResponseDto;
import com.example.commerce_mvp.application.user.UserPrincipal;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final UserRepository userRepository;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
                            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
                            UserRepository userRepository){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.userRepository = userRepository;
    }

    public TokenResponseDto generateToken(String subject, Collection<? extends GrantedAuthority> authorities){
        String authoritiesString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        //Access Token 생성
        Date accessTokenExpiresIn = new Date(now + accessTokenExpiration);
        String accessToken = Jwts.builder()
                .setSubject(subject)
                .claim("auth", authoritiesString)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        //Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + refreshTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenResponseDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    //Jwt 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Authentication getAuthentication(String accessToken){
        try {
            //토큰 복호화
            Claims claims = parseClaims(accessToken);
            String email = claims.getSubject();

            // 데이터베이스에서 사용자 정보 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));

            // UserPrincipal 생성
            UserPrincipal userPrincipal = new UserPrincipal(user);

            return new UsernamePasswordAuthenticationToken(userPrincipal, "", userPrincipal.getAuthorities());
        } catch (Exception e) {
            log.error("JWT 토큰 인증 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("토큰 인증에 실패했습니다.", e);
        }
    }

    //토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    // Refresh Token 검증 메서드
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid Refresh Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired Refresh Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported Refresh Token", e);
        } catch (IllegalArgumentException e) {
            log.info("Refresh Token claims string is empty.", e);
        }
        return false;
    }

    // Refresh Token으로부터 사용자 정보 추출
    public String getSubjectFromRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken);
        return claims.getSubject();
    }

    // Access Token 재발급
    public String generateAccessToken(String subject, Collection<? extends GrantedAuthority> authorities) {
        String authoritiesString = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(subject)
                .claim("auth", authoritiesString)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 새로운 Refresh Token 생성
    public String generateRefreshToken() {
        long now = (new Date()).getTime();
        return Jwts.builder()
                .setExpiration(new Date(now + refreshTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
