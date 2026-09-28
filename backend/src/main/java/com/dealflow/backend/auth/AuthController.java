package com.dealflow.backend.auth;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dealflow.backend.user.AppUser;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final CurrentUserService currentUserService;

    public AuthController(
            AuthenticationService authenticationService,
            CurrentUserService currentUserService) {
        this.authenticationService = authenticationService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request.email(), request.password());
    }

    @GetMapping("/me")
    public CurrentUserResponse me() {
        AppUser user = currentUserService.getCurrentUser();
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new CurrentUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                roles
        );
    }
}
