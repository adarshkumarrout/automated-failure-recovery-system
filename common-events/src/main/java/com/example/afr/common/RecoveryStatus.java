package com.example.afr.common;

public enum RecoveryStatus {
    RECEIVED,
    PROCESSING,
    SUCCEEDED,
    RETRY_PENDING,
    RETRYING,
    DEAD_LETTER,
    PERMANENT_FAILURE
}

