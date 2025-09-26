package com.example.commerce_mvp.application.user;

import com.example.commerce_mvp.application.user.dto.OAuthAttributes;
import com.example.commerce_mvp.application.user.dto.SessionUser;
import com.example.commerce_mvp.domain.user.User;
import com.example.commerce_mvp.domain.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuthUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());
        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user));
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getValue())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    @Transactional
    public User saveOrUpdate(OAuthAttributes attributes){
        User user = userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
                .map(entity -> entity.update(attributes.getUsername())) //기존 사용자인 경우 이름만 업데이트
                .orElse(attributes.toEntity()); //신규 사용자인 경우 생성
        return userRepository.save(user);
    }
}
