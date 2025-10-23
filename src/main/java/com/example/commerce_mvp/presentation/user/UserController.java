package com.example.commerce_mvp.presentation.user;

import com.example.commerce_mvp.application.user.UserPrincipal;
import com.example.commerce_mvp.application.user.UserService;
import com.example.commerce_mvp.application.user.dto.UserProfileResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getMyProfile(@AuthenticationPrincipal UserPrincipal currentUser){
        if (currentUser == null) {
            log.error("인증된 사용자 정보가 없습니다.");
            return ResponseEntity.status(401).build();
        }
        
        try {
            log.info("사용자 프로필 조회 요청 - 사용자: {}", currentUser.getEmail());
            UserProfileResponseDto myProfile = userService.getMyProfile(currentUser.getEmail());
            return ResponseEntity.ok(myProfile);
        } catch (Exception e) {
            log.error("사용자 프로필 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
