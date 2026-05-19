package com.example.afr.common;

public final class RecoveryTopics {

    public static final String API_FAILED = "api-failed-events";
    public static final String RETRY_SCHEDULED = "retry-scheduled-events";
    public static final String RETRY_SUCCEEDED = "retry-succeeded-events";
    public static final String RETRY_FAILED = "retry-failed-events";
    public static final String DEAD_LETTER = "dead-letter-events";

    private RecoveryTopics() {
    }
}

