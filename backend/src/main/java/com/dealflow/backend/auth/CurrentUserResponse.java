package com.dealflow.backend.auth;

import java.util.Set;

public record CurrentUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<String> roles
) {
}