package com.example.commerce_mvp.config;

import com.example.commerce_mvp.application.user.CustomOAuthUserService;
import com.example.commerce_mvp.application.user.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuthUserService customOAuthUserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        //아래 경로들은 인증 없이 접근 허용
                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll()
                        //아래 경로들은 인증이 필요
                        .requestMatchers("/api/**").authenticated()
                        //나머지 경로들은 모두 허용
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2->oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuthUserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
