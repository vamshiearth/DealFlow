package com.dealflow.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dealflow.backend.user.AppUser;
import com.dealflow.backend.user.AppUserRepository;
import com.dealflow.backend.user.Role;
import com.dealflow.backend.user.RoleName;
import com.dealflow.backend.user.UserStatus;

class AuthenticationServiceTest {

    @Test
    void shouldLoginValidActiveUserAndReturnJwtMetadata() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        JwtService jwtService = mock(JwtService.class);

        AppUser user = new AppUser();
        user.setId(7L);
        user.setEmail("manager@dealflow.local");
        user.setPasswordHash(encoder.encode("Password123!"));
        user.setStatus(UserStatus.ACTIVE);

        Role role = new Role();
        role.setName(RoleName.SALES_MANAGER);
        user.getRoles().add(role);

        when(repository.findByEmail("manager@dealflow.local")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthenticationService service = new AuthenticationService(repository, encoder, jwtService);

        LoginResponse response = service.login("manager@dealflow.local", "Password123!");

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        assertEquals(7L, response.userId());
        assertEquals("manager@dealflow.local", response.email());
        assertEquals(Set.of("SALES_MANAGER"), response.roles());
    }

    @Test
    void shouldRejectUnknownEmail() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        JwtService jwtService = mock(JwtService.class);

        when(repository.findByEmail("missing@dealflow.local")).thenReturn(Optional.empty());

        AuthenticationService service = new AuthenticationService(repository, encoder, jwtService);

        assertThrows(BadCredentialsException.class,
                () -> service.login("missing@dealflow.local", "Password123!"));
    }

    @Test
    void shouldRejectWrongPassword() {
        AppUserRepository repository = mock(AppUserRepository.class);
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        JwtService jwtService = mock(JwtService.class);

        AppUser user = new AppUser();
        user.setEmail("sales@dealflow.local");
        user.setPasswordHash(encoder.encode("CorrectPassword!"));
        user.setStatus(UserStatus.ACTIVE);

        when(repository.findByEmail("sales@dealflow.local")).thenReturn(Optional.of(user));

        AuthenticationService service = new AuthenticationService(repository, encoder, jwtService);

        assertThrows(BadCredentialsException.class,
                () -> service.login("sales@dealflow.local", "WrongPassword!"));
    }
}
