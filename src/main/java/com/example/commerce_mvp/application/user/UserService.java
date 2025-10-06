package com.example.commerce_mvp.application.user;

import com.example.commerce_mvp.application.user.dto.UserProfileResponseDto;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public UserProfileResponseDto getMyProfile(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("User not found with email: " + email));
        return UserProfileResponseDto.from(user);
    }
}
