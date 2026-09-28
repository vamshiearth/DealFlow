package com.dealflow.backend.auth;

import java.util.Set;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String email,
        Set<String> roles
) {
}
