package com.schambeck.marketplace.security;

import com.schambeck.marketplace.usecase.user.User;
import com.schambeck.marketplace.usecase.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationEvents {
    private final AuthenticationService authenticationService;
    private final UserService userService;
    
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        Authentication authentication = success.getAuthentication();
        String username = authenticationService.getLoggedUsername(authentication);
        if (!userService.existsByUsername(username)) {
            String email = authenticationService.getEmail(authentication);
            String name = authenticationService.getName(authentication);
            log.info("Creating user {} with e-mail {}", username, email);
            userService.create(User.builder()
                    .externalId(authentication.getName())
                    .username(username)
                    .email(email)
                    .fullName(name)
                    .build());
        }
    }
}
