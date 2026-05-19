package com.example.afr.worker;

import com.example.afr.common.RecoveryRequestEntity;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClient(RestClient.Builder builder, @Value("${payment.base-url}") String paymentBaseUrl) {
        this.restClient = builder.baseUrl(paymentBaseUrl).build();
    }

    @CircuitBreaker(name = "paymentService")
    public PaymentExecutionResponse execute(RecoveryRequestEntity request) {
        PaymentExecutionRequest body = new PaymentExecutionRequest(
                request.getIdempotencyKey(),
                request.getPayload(),
                request.getFailureType().name(),
                request.getAttemptCount(),
                request.getFailuresBeforeSuccess()
        );
        return restClient.post()
                .uri("/api/payments/execute")
                .body(body)
                .retrieve()
                .body(PaymentExecutionResponse.class);
    }

    public record PaymentExecutionRequest(
            String idempotencyKey,
            String payload,
            String failureMode,
            int attemptCount,
            int failuresBeforeSuccess
    ) {
    }

    public record PaymentExecutionResponse(boolean success, boolean retryable, String message) {
    }
}

