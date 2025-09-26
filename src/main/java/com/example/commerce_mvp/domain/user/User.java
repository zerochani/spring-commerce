package com.example.commerce_mvp.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "providerId"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;

    @Column(nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Builder
    public User(String email, String username, SocialProvider provider, String providerId, UserRole role){
        this.email = email;
        this.username = username;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }

    public User update(String username){
        this.username = username;
        return this;
    }
}
