package com.dealflow.backend.auth;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import com.dealflow.backend.user.AppUser;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMs;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMs = expirationMs;
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationMs);

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("dealflow")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("roles", roles)
                .build();

        return jwtEncoder
            .encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
            ))
                .getTokenValue();
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
