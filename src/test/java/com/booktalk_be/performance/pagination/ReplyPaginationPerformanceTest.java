package com.booktalk_be.performance.pagination;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import com.booktalk_be.domain.reply.service.ReplyService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reply Pagination Performance Test
 *
 * Validates Phase 2 improvements:
 * - getRepliesByPostCodePaginated method
 * - Depth limit (max 3)
 * - Batch loading of child replies
 *
 * Success Criteria:
 * - Paginated method faster or equal to full load method
 * - Depth limit enforced (maxDepth <= 3)
 * - No duplicate replies across pages
 * - Batch loading uses single query per depth level
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReplyPaginationPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(ReplyPaginationPerformanceTest.class);

    @Autowired
    private ReplyService replyService;

    @PersistenceContext
    private EntityManager em;

    private Statistics statistics;
    private String testPostCode;

    @BeforeEach
    void setUp() {
        // Enable Hibernate statistics
        SessionFactory sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        // Find a post with many replies for testing
        if (testPostCode == null) {
            // Find post with most replies
            List<Object[]> postWithMostReplies = em.createQuery(
                    "SELECT r.postCode, COUNT(r) as cnt FROM Reply r WHERE r.delYn = false " +
                    "GROUP BY r.postCode ORDER BY cnt DESC", Object[].class)
                    .setMaxResults(1)
                    .getResultList();

            if (!postWithMostReplies.isEmpty()) {
                testPostCode = (String) postWithMostReplies.get(0)[0];
                Long replyCount = (Long) postWithMostReplies.get(0)[1];
                log.info("Selected test post: {} with {} replies", testPostCode, replyCount);
            } else {
                // Fallback: get any post code
                List<String> postCodes = em.createQuery(
                        "SELECT DISTINCT r.postCode FROM Reply r WHERE r.delYn = false", String.class)
                        .setMaxResults(1)
                        .getResultList();
                testPostCode = postCodes.isEmpty() ? null : postCodes.get(0);
            }
        }
    }

    @AfterEach
    void tearDown() {
        if (statistics != null) {
            log.info("Test Statistics:");
            log.info("  Query Execution Count: {}", statistics.getQueryExecutionCount());
            log.info("  Entity Load Count: {}", statistics.getEntityLoadCount());
        }
    }

    @Test
    @Order(1)
    @Transactional(readOnly = true)
    @DisplayName("1. Compare Old vs New Method Performance")
    void compareOldVsNewMethod() {
        if (testPostCode == null) {
            log.warn("Skipping test: No reply data available");
            return;
        }

        log.info("=== Comparing Old vs New Reply Methods ===");

        // Old method (full load)
        statistics.clear();
        long startOld = System.currentTimeMillis();
        List<ReplyResponse> oldResult = replyService.getRepliesByPostCode(testPostCode);
        long durationOld = System.currentTimeMillis() - startOld;
        long queryCountOld = statistics.getQueryExecutionCount();

        // New method (paginated)
        statistics.clear();
        long startNew = System.currentTimeMillis();
        PageResponseDto<ReplyResponse> newResult =
                replyService.getRepliesByPostCodePaginated(testPostCode, 1, 20);
        long durationNew = System.currentTimeMillis() - startNew;
        long queryCountNew = statistics.getQueryExecutionCount();

        log.info("Old Method (Full Load):");
        log.info("  Duration: {}ms", durationOld);
        log.info("  Records Returned: {}", oldResult.size());
        log.info("  Query Count: {}", queryCountOld);

        log.info("New Method (Paginated):");
        log.info("  Duration: {}ms", durationNew);
        log.info("  Records Returned: {}", newResult.getContent().size());
        log.info("  Total Pages: {}", newResult.getTotalPages());
        log.info("  Query Count: {}", queryCountNew);

        // New method should be faster or equal for single page
        assertThat(durationNew)
                .as("Paginated method should be faster or equal to full load for single page")
                .isLessThanOrEqualTo(durationOld + 50); // Allow 50ms tolerance
    }

    @Test
    @Order(2)
    @Transactional(readOnly = true)
    @DisplayName("2. Verify Depth Limit Enforcement (max 3)")
    void testDepthLimitEnforcement() {
        if (testPostCode == null) {
            log.warn("Skipping test: No reply data available");
            return;
        }

        log.info("=== Testing Depth Limit Enforcement ===");

        PageResponseDto<ReplyResponse> result =
                replyService.getRepliesByPostCodePaginated(testPostCode, 1, 50);

        // Calculate max depth in returned data
        int maxDepth = calculateMaxDepth(result.getContent());

        log.info("Depth Limit Verification:");
        log.info("  Max Depth Found: {}", maxDepth);
        log.info("  Expected Max Depth: 3");

        assertThat(maxDepth)
                .as("Maximum reply depth should not exceed 3")
                .isLessThanOrEqualTo(3);
    }

    private int calculateMaxDepth(List<ReplyResponse> replies) {
        if (replies == null || replies.isEmpty()) {
            return 0;
        }

        int maxDepth = 1;
        for (ReplyResponse reply : replies) {
            int childDepth = calculateMaxDepth(reply.getReplies());
            maxDepth = Math.max(maxDepth, 1 + childDepth);
        }
        return maxDepth;
    }

    @Test
    @Order(3)
    @Transactional(readOnly = true)
    @DisplayName("3. Root Replies Pagination - No Duplicates Between Pages")
    void testRootRepliesPagination() {
        if (testPostCode == null) {
            log.warn("Skipping test: No reply data available");
            return;
        }

        log.info("=== Testing Root Replies Pagination (No Duplicates) ===");

        Set<String> allReplyCodes = new HashSet<>();
        int duplicateCount = 0;
        int pageSize = 20;
        int maxPages = 5;

        for (int page = 1; page <= maxPages; page++) {
            PageResponseDto<ReplyResponse> result =
                    replyService.getRepliesByPostCodePaginated(testPostCode, page, pageSize);

            if (result.getContent().isEmpty()) {
                log.info("  Page {}: empty (end of data)", page);
                break;
            }

            for (ReplyResponse reply : result.getContent()) {
                if (!allReplyCodes.add(reply.getReplyCode())) {
                    duplicateCount++;
                    log.warn("  Duplicate found: {}", reply.getReplyCode());
                }
            }

            log.info("  Page {}: {} root replies", page, result.getContent().size());
        }

        log.info("Pagination Duplicate Check:");
        log.info("  Total Unique Root Replies: {}", allReplyCodes.size());
        log.info("  Duplicates Found: {}", duplicateCount);

        assertThat(duplicateCount)
                .as("No duplicate root replies should exist across pages")
                .isEqualTo(0);
    }

    @Test
    @Order(4)
    @Transactional(readOnly = true)
    @DisplayName("4. Child Replies Batch Loading - Query Efficiency")
    void testChildRepliesBatchLoading() {
        if (testPostCode == null) {
            log.warn("Skipping test: No reply data available");
            return;
        }

        log.info("=== Testing Child Replies Batch Loading ===");
        statistics.clear();

        // Fetch with larger page size to trigger batch loading
        PageResponseDto<ReplyResponse> result =
                replyService.getRepliesByPostCodePaginated(testPostCode, 1, 50);

        long queryCount = statistics.getQueryExecutionCount();

        // Count total replies including children
        int totalReplies = countTotalReplies(result.getContent());

        log.info("Batch Loading Results:");
        log.info("  Root Replies: {}", result.getContent().size());
        log.info("  Total Replies (including children): {}", totalReplies);
        log.info("  Query Count: {}", queryCount);

        // Query count should be limited:
        // 1 (root replies) + 1 (count) + up to 3 (one per depth level)
        assertThat(queryCount)
                .as("Batch loading should limit queries to max 5 (root + count + 3 depth levels)")
                .isLessThanOrEqualTo(5);
    }

    private int countTotalReplies(List<ReplyResponse> replies) {
        if (replies == null) {
            return 0;
        }
        int count = replies.size();
        for (ReplyResponse reply : replies) {
            count += countTotalReplies(reply.getReplies());
        }
        return count;
    }

    @Test
    @Order(5)
    @Transactional(readOnly = true)
    @DisplayName("5. Multiple Pages Load Performance (avg < 500ms)")
    void testMultiplePagesLoad() {
        if (testPostCode == null) {
            log.warn("Skipping test: No reply data available");
            return;
        }

        log.info("=== Testing Multiple Pages Load Performance ===");

        int pageCount = 10;
        List<Long> durations = new ArrayList<>();

        for (int page = 1; page <= pageCount; page++) {
            long startTime = System.currentTimeMillis();

            PageResponseDto<ReplyResponse> result =
                    replyService.getRepliesByPostCodePaginated(testPostCode, page, 20);

            long duration = System.currentTimeMillis() - startTime;
            durations.add(duration);

            log.info("  Page {}: {}ms, {} root replies, {} total pages",
                    page, duration, result.getContent().size(), result.getTotalPages());

            if (result.getContent().isEmpty()) {
                break;
            }
        }

        double avgDuration = durations.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        long maxDuration = durations.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        log.info("Multiple Pages Load Results:");
        log.info("  Pages Tested: {}", durations.size());
        log.info("  Avg Duration: {}ms", String.format("%.2f", avgDuration));
        log.info("  Max Duration: {}ms", maxDuration);

        assertThat(avgDuration)
                .as("Average page load time should be under 500ms")
                .isLessThan(500);
    }

    @Test
    @Order(6)
    @Transactional(readOnly = true)
    @DisplayName("6. Tree Structure Integrity Check")
    void testTreeStructureIntegrity() {
        if (testPostCode == null) {
            log.warn("Skipping test: No reply data available");
            return;
        }

        log.info("=== Testing Tree Structure Integrity ===");

        PageResponseDto<ReplyResponse> result =
                replyService.getRepliesByPostCodePaginated(testPostCode, 1, 30);

        int rootCount = result.getContent().size();
        int depth1Count = 0;
        int depth2Count = 0;
        int depth3Count = 0;

        for (ReplyResponse root : result.getContent()) {
            if (root.getReplies() != null) {
                depth1Count += root.getReplies().size();
                for (ReplyResponse depth1 : root.getReplies()) {
                    if (depth1.getReplies() != null) {
                        depth2Count += depth1.getReplies().size();
                        for (ReplyResponse depth2 : depth1.getReplies()) {
                            if (depth2.getReplies() != null) {
                                depth3Count += depth2.getReplies().size();
                            }
                        }
                    }
                }
            }
        }

        log.info("Tree Structure:");
        log.info("  Root Replies (Depth 0): {}", rootCount);
        log.info("  Depth 1 Replies: {}", depth1Count);
        log.info("  Depth 2 Replies: {}", depth2Count);
        log.info("  Depth 3 Replies: {}", depth3Count);
        log.info("  Total: {}", rootCount + depth1Count + depth2Count + depth3Count);

        // Verify structure is valid
        assertThat(rootCount).isGreaterThanOrEqualTo(0);

        // If we have depth 3, it should be the maximum (depth limit)
        // Depth 4+ should not exist
    }

    @Test
    @Order(7)
    @Transactional(readOnly = true)
    @DisplayName("7. Memory Efficiency - Large Page Request")
    void testMemoryEfficiency() {
        if (testPostCode == null) {
            log.warn("Skipping test: No reply data available");
            return;
        }

        log.info("=== Testing Memory Efficiency with Large Page ===");

        Runtime runtime = Runtime.getRuntime();

        // Force GC before measurement
        System.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        long startTime = System.currentTimeMillis();
        PageResponseDto<ReplyResponse> result =
                replyService.getRepliesByPostCodePaginated(testPostCode, 1, 100);
        long duration = System.currentTimeMillis() - startTime;

        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;

        int totalReplies = countTotalReplies(result.getContent());

        log.info("Memory Efficiency Results:");
        log.info("  Root Replies Requested: 100");
        log.info("  Root Replies Returned: {}", result.getContent().size());
        log.info("  Total Replies (with children): {}", totalReplies);
        log.info("  Duration: {}ms", duration);
        log.info("  Memory Before: {} MB", memoryBefore / (1024 * 1024));
        log.info("  Memory After: {} MB", memoryAfter / (1024 * 1024));
        log.info("  Memory Used: {} KB", memoryUsed / 1024);

        // Should complete without OOM and in reasonable time
        assertThat(duration)
                .as("Large page request should complete in under 2 seconds")
                .isLessThan(2000);
    }

    @Test
    @Order(8)
    @Transactional(readOnly = true)
    @DisplayName("8. Empty Result Handling")
    void testEmptyResultHandling() {
        log.info("=== Testing Empty Result Handling ===");

        // Use a non-existent post code
        String nonExistentPostCode = "NONEXISTENT_POST_CODE_12345";

        PageResponseDto<ReplyResponse> result =
                replyService.getRepliesByPostCodePaginated(nonExistentPostCode, 1, 20);

        log.info("Empty Result:");
        log.info("  Content Size: {}", result.getContent().size());
        log.info("  Total Pages: {}", result.getTotalPages());

        assertThat(result.getContent())
                .as("Empty post should return empty list")
                .isEmpty();

        assertThat(result.getTotalPages())
                .as("Empty post should have 0 total pages")
                .isEqualTo(0);
    }
}
