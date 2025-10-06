package com.example.commerce_mvp.presentation.user;

import com.example.commerce_mvp.application.user.UserService;
import com.example.commerce_mvp.application.user.dto.UserProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getMyProfile(@AuthenticationPrincipal UserDetails userDetails){
        String email = userDetails.getUsername();
        UserProfileResponseDto myProfile = userService.getMyProfile(email);
        return ResponseEntity.ok(myProfile);
    }
}
