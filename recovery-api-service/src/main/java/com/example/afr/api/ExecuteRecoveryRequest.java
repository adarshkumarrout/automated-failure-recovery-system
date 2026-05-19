package com.example.afr.api;

import com.example.afr.common.FailureType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExecuteRecoveryRequest(
        @NotBlank String idempotencyKey,
        @NotBlank String operationType,
        @NotBlank String payload,
        @NotNull FailureType failureMode,
        int failuresBeforeSuccess
) {
}

