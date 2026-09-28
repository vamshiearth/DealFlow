package com.dealflow.backend.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class DealFlowJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter delegate;

    public DealFlowJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        this.delegate = new JwtAuthenticationConverter();
        this.delegate.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return delegate.convert(jwt);
    }
}
