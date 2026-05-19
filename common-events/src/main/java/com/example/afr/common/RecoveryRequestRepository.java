package com.example.afr.common;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecoveryRequestRepository extends JpaRepository<RecoveryRequestEntity, Long> {

    Optional<RecoveryRequestEntity> findByIdempotencyKey(String idempotencyKey);

    List<RecoveryRequestEntity> findTop50ByOrderByUpdatedAtDesc();

    List<RecoveryRequestEntity> findTop100ByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
            RecoveryStatus status,
            Instant now
    );
}

