package com.dealflow.backend.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dealflow.backend.user.AppUser;
import com.dealflow.backend.user.AppUserRepository;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String requiredAuthority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(requiredAuthority));
    }

    public String getCurrentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        return authentication.getName();
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user is available.");
        }

        String email = authentication.getName();
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user " + email + " was not found."
                ));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
