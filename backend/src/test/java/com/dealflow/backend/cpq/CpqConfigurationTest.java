package com.dealflow.backend.cpq;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CpqConfigurationTest {

    @Test
    void defaultsToDisabledWithoutCredentials() {
        CpqConfiguration configuration = new CpqConfiguration(false, "", "", "");

        assertFalse(configuration.enabled());
        assertEquals("", configuration.baseUrl());
        assertEquals("", configuration.username());
        assertEquals("", configuration.password());
    }
}
