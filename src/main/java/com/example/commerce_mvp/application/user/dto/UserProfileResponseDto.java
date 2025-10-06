package com.example.commerce_mvp.application.user.dto;

import com.example.commerce_mvp.domain.user.SocialProvider;
import com.example.commerce_mvp.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserProfileResponseDto {

    private final String email;
    private final String username;
    private final SocialProvider provider;

    @Builder
    public UserProfileResponseDto(String email, String username, SocialProvider provider){
        this.email = email;
        this.username = username;
        this.provider = provider;
    }

    public static UserProfileResponseDto from(User user){
        return UserProfileResponseDto.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .provider(user.getProvider())
                .build();
    }

}
