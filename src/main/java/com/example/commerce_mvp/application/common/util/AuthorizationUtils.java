package com.example.commerce_mvp.application.common.util;

import com.example.commerce_mvp.application.common.exception.BusinessException;
import com.example.commerce_mvp.application.common.exception.ErrorCode;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthorizationUtils {

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "인증되지 않은 사용자입니다.");
        }
        return (User) authentication.getPrincipal();
    }

    public static void validateAdminRole() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "관리자 권한이 필요합니다.");
        }
    }

    public static void validateUserOwnership(String userEmail) {
        User currentUser = getCurrentUser();
        if (!currentUser.getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인의 리소스만 접근할 수 있습니다.");
        }
    }
}
