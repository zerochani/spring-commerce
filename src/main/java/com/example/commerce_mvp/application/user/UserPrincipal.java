package com.example.commerce_mvp.application.user;

import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class UserPrincipal implements UserDetails, OAuth2User {
    
    private final User user;
    private final Map<String, Object> attributes;
    
    public UserPrincipal(User user) {
        this.user = user;
        this.attributes = Collections.emptyMap();
    }
    
    public UserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getValue()));
    }
    
    @Override
    public String getPassword() {
        return null; // OAuth2에서는 패스워드가 없음
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    public String getEmail() {
        return user.getEmail();
    }
    
    public UserRole getRole() {
        return user.getRole();
    }
    
    // OAuth2User 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public String getName() {
        return user.getEmail();
    }
}
