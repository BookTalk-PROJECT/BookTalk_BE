package com.booktalk_be.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Distributed ID Generator using Snowflake-like algorithm.
 * Generates unique IDs across multiple instances without coordination.
 *
 * ID Structure (64-bit):
 * - 1 bit: sign (always 0)
 * - 41 bits: timestamp (milliseconds since EPOCH) - ~69 years
 * - 10 bits: worker ID (0-1023) - supports up to 1024 instances
 * - 12 bits: sequence (0-4095) - supports 4096 IDs per millisecond per worker
 */
@Component
public class DistributedIdGenerator {

    private static final long EPOCH = 1704067200000L; // 2024-01-01 00:00:00 UTC
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS); // 1023
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS); // 4095

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public DistributedIdGenerator(@Value("${app.worker-id:0}") long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                String.format("Worker ID must be between 0 and %d, got: %d", MAX_WORKER_ID, workerId));
        }
        this.workerId = workerId;
    }

    /**
     * Generate unique Board ID with "BO_" prefix
     */
    public synchronized String generateBoardId() {
        return "BO_" + nextId();
    }

    /**
     * Generate unique Reply ID with "REP_" prefix
     */
    public synchronized String generateReplyId() {
        return "REP_" + nextId();
    }

    /**
     * Generate unique BookReview ID with "BR_" prefix
     */
    public synchronized String generateBookReviewId() {
        return "BR_" + nextId();
    }

    /**
     * Generate unique ID with custom prefix
     */
    public synchronized String generateId(String prefix) {
        return prefix + nextId();
    }

    /**
     * Generate raw unique ID (no prefix)
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        if (timestamp < lastTimestamp) {
            // Clock moved backwards - wait until we catch up
            timestamp = waitNextMillis(lastTimestamp);
        }

        if (timestamp == lastTimestamp) {
            // Same millisecond - increment sequence
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // Sequence overflow - wait for next millisecond
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // New millisecond - reset sequence
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Extract timestamp from generated ID (for debugging/analysis)
     */
    public static long extractTimestamp(long id) {
        return (id >> TIMESTAMP_SHIFT) + EPOCH;
    }

    /**
     * Extract worker ID from generated ID (for debugging/analysis)
     */
    public static long extractWorkerId(long id) {
        return (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }

    /**
     * Extract sequence from generated ID (for debugging/analysis)
     */
    public static long extractSequence(long id) {
        return id & MAX_SEQUENCE;
    }
}
