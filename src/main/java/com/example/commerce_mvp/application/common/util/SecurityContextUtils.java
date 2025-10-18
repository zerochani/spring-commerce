package com.example.commerce_mvp.application.common.util;

import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import com.example.commerce_mvp.application.user.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextUtils {

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "인증된 사용자 정보가 없습니다.");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getEmail();
        } else {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "인증된 사용자 정보 형식이 올바르지 않습니다.");
        }
    }

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "인증된 사용자 정보가 없습니다.");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        } else {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "인증된 사용자 정보 형식이 올바르지 않습니다.");
        }
    }
}
