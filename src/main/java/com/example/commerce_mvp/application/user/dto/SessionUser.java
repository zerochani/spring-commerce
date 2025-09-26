package com.example.commerce_mvp.application.user.dto;

import com.example.commerce_mvp.domain.user.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private final String username;
    private final String email;

    public SessionUser(User user){
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
}
