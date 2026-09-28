package com.dealflow.backend.cpq;

import java.util.Map;

public record CpqConfigurationDTO(
        String productFamily,
        String model,
        Map<String, String> attributes
) {
}