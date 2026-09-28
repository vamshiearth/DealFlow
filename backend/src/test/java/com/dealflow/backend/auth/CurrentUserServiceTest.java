package com.dealflow.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dealflow.backend.user.AppUser;
import com.dealflow.backend.user.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesAuthenticatedApplicationUserFromEmail() {
        AppUser user = new AppUser();
        user.setId(7L);
        user.setEmail("alice@example.com");
        when(appUserRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(user));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "alice@example.com",
                        "token",
                        List.of(new SimpleGrantedAuthority("ROLE_SALES_REP"))
                )
        );

        CurrentUserService service = new CurrentUserService(appUserRepository);

        assertEquals(user, service.getCurrentUser());
        assertEquals(7L, service.getCurrentUserId());
    }

    @Test
    void rejectsMissingAuthentication() {
        CurrentUserService service = new CurrentUserService(appUserRepository);

        assertThrows(IllegalStateException.class, service::getCurrentUser);
    }
}
