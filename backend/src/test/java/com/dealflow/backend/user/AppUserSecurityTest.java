package com.dealflow.backend.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AppUserSecurityTest {

    @Test
    void shouldAllowRoleAndPasswordHashSetupForDealFlowUser() {
        Role role = new Role();
        role.setName(RoleName.SALES_REP);

        AppUser user = new AppUser();
        user.setEmail("alice@dealflow.com");
        user.setFirstName("Alice");
        user.setLastName("Seller");

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPasswordHash(encoder.encode("Password123!"));
        user.getRoles().add(role);

        assertEquals("alice@dealflow.com", user.getEmail());
        assertEquals(RoleName.SALES_REP, user.getRoles().iterator().next().getName());
        assertTrue(encoder.matches("Password123!", user.getPasswordHash()));
    }
}
