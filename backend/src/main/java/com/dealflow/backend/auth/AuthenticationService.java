package com.dealflow.backend.auth;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dealflow.backend.user.AppUser;
import com.dealflow.backend.user.AppUserRepository;
import com.dealflow.backend.user.UserStatus;

@Service
public class AuthenticationService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String email, String password) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user);

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getId(),
                user.getEmail(),
                roles
        );
    }
}
