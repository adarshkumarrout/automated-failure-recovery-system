package com.example.afr.payment;

import com.example.afr.common.FailureType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping("/execute")
    public PaymentExecutionResponse execute(@Valid @RequestBody PaymentExecutionRequest request) {
        FailureType failureType = FailureType.valueOf(request.failureMode());

        if (failureType == FailureType.PERMANENT) {
            return new PaymentExecutionResponse(false, false, "Payment permanently rejected");
        }

        if (failureType == FailureType.TEMPORARY
                && request.attemptCount() <= request.failuresBeforeSuccess()) {
            return new PaymentExecutionResponse(false, true, "Payment provider temporary timeout");
        }

        return new PaymentExecutionResponse(true, false, "Payment executed successfully");
    }

    public record PaymentExecutionRequest(
            @NotBlank String idempotencyKey,
            @NotBlank String payload,
            @NotBlank String failureMode,
            @Min(1) int attemptCount,
            @Min(0) int failuresBeforeSuccess
    ) {
    }

    public record PaymentExecutionResponse(boolean success, boolean retryable, String message) {
    }
}

