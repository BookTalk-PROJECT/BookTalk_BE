package com.booktalk_be.performance.comparison;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.common.utils.DistributedIdGenerator;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.model.repository.ReplyRepository;
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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Before/After Comparison Test
 *
 * Performs actual rollback-style comparisons for Phase 1~4 improvements.
 * Each test simulates the "before" state and compares it with the current "after" state.
 *
 * Phase 1: N+1 Query Resolution (fetchJoin) + @Formula Subquery Removal
 * Phase 2: Reply Pagination (full load vs paginated)
 * Phase 3: Index Optimization (DROP → measure → CREATE → measure)
 * Phase 4: Concurrency Control (System.currentTimeMillis() vs DistributedIdGenerator)
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeforeAfterComparisonTest {

    private static final Logger log = LoggerFactory.getLogger(BeforeAfterComparisonTest.class);

    @Autowired
    private ReplyService replyService;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private DistributedIdGenerator idGenerator;

    @PersistenceContext
    private EntityManager em;

    private Statistics statistics;
    private String testPostCode;
    private Integer testCategoryId;

    @BeforeEach
    void setUp() {
        SessionFactory sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        if (testPostCode == null) {
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
                List<String> postCodes = em.createQuery(
                        "SELECT DISTINCT r.postCode FROM Reply r WHERE r.delYn = false", String.class)
                        .setMaxResults(1)
                        .getResultList();
                testPostCode = postCodes.isEmpty() ? null : postCodes.get(0);
            }
        }

        if (testCategoryId == null) {
            List<Integer> categoryIds = em.createQuery(
                    "SELECT c.categoryId FROM Category c WHERE c.delYn = false", Integer.class)
                    .setMaxResults(1)
                    .getResultList();
            testCategoryId = categoryIds.isEmpty() ? 1 : categoryIds.get(0);
        }
    }

    // =========================================================================
    // Phase 1-1: N+1 Query Comparison
    // =========================================================================

    @Test
    @Order(1)
    @Transactional(readOnly = true)
    @DisplayName("Phase 1-1: N+1 Query - fetchJoin Before/After Comparison")
    void phase1_N1QueryComparison() {
        if (testPostCode == null) {
            log.warn("Skipping test: No reply data available");
            return;
        }

        log.info("============================================================");
        log.info("=== Phase 1-1: N+1 Query Before/After Comparison ===");
        log.info("============================================================");

        // === BEFORE: Without fetchJoin ===
        statistics.clear();
        @SuppressWarnings("unchecked")
        List<Reply> repliesWithoutFetchJoin = em.createQuery(
                "SELECT r FROM Reply r WHERE r.postCode = :postCode AND r.delYn = false " +
                "ORDER BY r.regTime ASC",
                Reply.class)
                .setParameter("postCode", testPostCode)
                .setMaxResults(50)
                .getResultList();
        long queryCountBeforeAccess = statistics.getQueryExecutionCount();

        // Access member.getName() to trigger lazy loading
        int distinctMemberCount = 0;
        Set<Integer> accessedMemberIds = new HashSet<>();
        for (Reply r : repliesWithoutFetchJoin) {
            try {
                if (r.getMember() != null) {
                    String name = r.getMember().getName();
                    accessedMemberIds.add(r.getMember().getMemberId());
                }
            } catch (Exception e) {
                // ignore lazy init exceptions
            }
        }
        distinctMemberCount = accessedMemberIds.size();
        long queryCountAfterAccess = statistics.getQueryExecutionCount();
        long n1TotalQueries = queryCountAfterAccess;
        long n1ExtraQueries = queryCountAfterAccess - queryCountBeforeAccess;

        // === AFTER: With fetchJoin (current code) ===
        statistics.clear();
        List<Reply> repliesWithFetchJoin = replyRepository.getRepliesByPostCode(testPostCode);
        long queryCountAfterFetchJoin = statistics.getQueryExecutionCount();

        // Access member.getName() - should NOT trigger additional queries
        for (Reply r : repliesWithFetchJoin) {
            try {
                if (r.getMember() != null) {
                    r.getMember().getName();
                }
            } catch (Exception e) {
                // ignore
            }
        }
        long queryCountAfterFetchJoinAccess = statistics.getQueryExecutionCount();

        // Theoretical N+1 calculation (without batch_fetch_size)
        int theoreticalN1Queries = 1 + distinctMemberCount;

        // Results
        log.info("--- Before (Without fetchJoin) ---");
        log.info("  Initial query count: {}", queryCountBeforeAccess);
        log.info("  After member access: {} (extra: {})", queryCountAfterAccess, n1ExtraQueries);
        log.info("  Total replies loaded: {}", repliesWithoutFetchJoin.size());
        log.info("  Distinct members accessed: {}", distinctMemberCount);
        log.info("");
        log.info("  [NOTE] hibernate.default_batch_fetch_size=100 is active.");
        log.info("  With batch fetching, Hibernate groups N lazy loads into ceil(N/100) IN queries.");
        log.info("  Observed extra queries: {} (batch-fetched {} members)", n1ExtraQueries, distinctMemberCount);
        log.info("  WITHOUT batch_fetch_size, this would be 1 + {} = {} queries (pure N+1).",
                distinctMemberCount, theoreticalN1Queries);

        log.info("--- After (With fetchJoin) ---");
        log.info("  Initial query count: {}", queryCountAfterFetchJoin);
        log.info("  After member access: {} (extra: {})",
                queryCountAfterFetchJoinAccess, queryCountAfterFetchJoinAccess - queryCountAfterFetchJoin);
        log.info("  Total replies loaded: {}", repliesWithFetchJoin.size());

        log.info("--- Improvement ---");
        log.info("  Observed: {} queries -> {} queries", n1TotalQueries, queryCountAfterFetchJoinAccess);
        log.info("  Theoretical (no batch_fetch_size): {} queries -> {} queries",
                theoreticalN1Queries, queryCountAfterFetchJoinAccess);
        double theoreticalImprovement = theoreticalN1Queries > 0
                ? (1.0 - (double) queryCountAfterFetchJoinAccess / theoreticalN1Queries) * 100.0
                : 0;
        log.info("  Theoretical improvement: {}%", String.format("%.1f", theoreticalImprovement));
        log.info("");
        log.info("  [WHY fetchJoin is better than batch_fetch_size alone]");
        log.info("  - batch_fetch_size=100: 1 main query + ceil(N/100) IN queries = {} round-trips",
                1 + (int) Math.ceil((double) distinctMemberCount / 100));
        log.info("  - fetchJoin: 1 JOIN query = 1 round-trip (always optimal)");
        log.info("  - fetchJoin eliminates all additional round-trips regardless of data size.");

        // fetchJoin should not produce extra queries on member access
        assertThat(queryCountAfterFetchJoinAccess)
                .as("fetchJoin should not trigger additional queries on member access")
                .isEqualTo(queryCountAfterFetchJoin);

        // fetchJoin should use fewer or equal total queries
        assertThat(queryCountAfterFetchJoinAccess)
                .as("fetchJoin total queries should be <= batch-fetched N+1 pattern")
                .isLessThanOrEqualTo(n1TotalQueries);
    }

    // =========================================================================
    // Phase 1-2: @Formula Subquery Removal + @Version Trade-off
    // =========================================================================

    @Test
    @Order(2)
    @Transactional(readOnly = true)
    @DisplayName("Phase 1-2: @Formula Subquery vs Stored Column Comparison")
    void phase1_FormulaSubqueryComparison() {
        if (testCategoryId == null) {
            log.warn("Skipping test: No category data available");
            return;
        }

        log.info("============================================================");
        log.info("=== Phase 1-2: @Formula Subquery vs Stored Column ===");
        log.info("============================================================");

        int iterations = 10;

        // === BEFORE: @Formula subquery simulation ===
        List<Long> durationsBefore = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            statistics.clear();
            long startBefore = System.nanoTime();
            @SuppressWarnings("unchecked")
            List<Object[]> withSubquery = em.createNativeQuery(
                    "SELECT b.code, b.title, " +
                    "(SELECT COUNT(1) FROM likes l WHERE l.code = b.code) as like_cnt " +
                    "FROM board b WHERE b.category_id = :catId AND b.del_yn = false " +
                    "ORDER BY b.reg_time DESC LIMIT 20")
                    .setParameter("catId", testCategoryId)
                    .getResultList();
            long durationBefore = (System.nanoTime() - startBefore) / 1_000_000;
            durationsBefore.add(durationBefore);
        }

        // === AFTER: Stored column (no subquery) ===
        List<Long> durationsAfter = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            statistics.clear();
            long startAfter = System.nanoTime();
            @SuppressWarnings("unchecked")
            List<Object[]> withStoredColumn = em.createNativeQuery(
                    "SELECT b.code, b.title, b.like_cnt " +
                    "FROM board b WHERE b.category_id = :catId AND b.del_yn = false " +
                    "ORDER BY b.reg_time DESC LIMIT 20")
                    .setParameter("catId", testCategoryId)
                    .getResultList();
            long durationAfter = (System.nanoTime() - startAfter) / 1_000_000;
            durationsAfter.add(durationAfter);
        }

        double avgBefore = durationsBefore.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgAfter = durationsAfter.stream().mapToLong(Long::longValue).average().orElse(0);
        double improvement = avgBefore > 0 ? (1.0 - avgAfter / avgBefore) * 100.0 : 0;

        log.info("--- Before (@Formula subquery, {}x avg) ---", iterations);
        log.info("  Average: {}ms", String.format("%.1f", avgBefore));
        log.info("  All runs: {}ms", durationsBefore);

        log.info("--- After (Stored column, {}x avg) ---", iterations);
        log.info("  Average: {}ms", String.format("%.1f", avgAfter));
        log.info("  All runs: {}ms", durationsAfter);

        log.info("--- Improvement ---");
        log.info("  {}ms -> {}ms ({}%)",
                String.format("%.1f", avgBefore),
                String.format("%.1f", avgAfter),
                String.format("%.1f", improvement));

        log.info("");
        log.info("  [TRADE-OFF: like_cnt stored column + @Version conflict]");
        log.info("  Storing like_cnt in Board requires UPDATE board SET like_cnt=like_cnt+1.");
        log.info("  This increments @Version, causing conflicts with concurrent title/content edits.");
        log.info("  Example: User A likes post (version 1->2), User B edits title (expects version 1) -> 409 Conflict.");
        log.info("  Mitigations:");
        log.info("    A) Use bulk UPDATE: UPDATE board SET like_cnt = like_cnt + 1 WHERE code = ? (bypasses @Version)");
        log.info("    B) Separate counter table (likes_count) to isolate like operations");
        log.info("    C) External cache (Redis) for high-frequency like counts");
    }

    // =========================================================================
    // Phase 1-3: @Version Conflict Demonstration
    // =========================================================================

    @Test
    @Order(3)
    @Transactional
    @DisplayName("Phase 1-3: @Version Conflict Trade-off Demonstration (like_cnt + edit)")
    void phase1_VersionConflictDemonstration() {
        log.info("============================================================");
        log.info("=== Phase 1-3: @Version Conflict Trade-off Demonstration ===");
        log.info("============================================================");

        // Check if a Board entity exists that we can use for the demonstration
        @SuppressWarnings("unchecked")
        List<Object[]> boards = em.createNativeQuery(
                "SELECT code, version, like_cnt, title FROM board WHERE del_yn = false LIMIT 1")
                .getResultList();

        if (boards.isEmpty()) {
            log.warn("Skipping test: No board data available");
            return;
        }

        Object[] boardRow = boards.get(0);
        String boardCode = (String) boardRow[0];
        long currentVersion = ((Number) boardRow[1]).longValue();
        int currentLikeCnt = ((Number) boardRow[2]).intValue();
        String currentTitle = (String) boardRow[3];

        log.info("  Test board: code={}, version={}, like_cnt={}", boardCode, currentVersion, currentLikeCnt);

        // Simulate scenario: likes update increments version, blocking a concurrent edit
        // Step 1: "User A reads board at version X"
        log.info("  Step 1: User A and User B both read board at version={}", currentVersion);

        // Step 2: "User A likes the post" - simulate with native UPDATE
        int likeUpdateRows = em.createNativeQuery(
                "UPDATE board SET like_cnt = like_cnt + 1, version = version + 1 WHERE code = :code AND version = :version")
                .setParameter("code", boardCode)
                .setParameter("version", currentVersion)
                .executeUpdate();
        em.flush();

        log.info("  Step 2: User A likes the post -> UPDATE with version={} -> {} rows affected",
                currentVersion, likeUpdateRows);

        // Step 3: "User B tries to edit the title" with the OLD version
        int editUpdateRows = em.createNativeQuery(
                "UPDATE board SET title = :title, version = version + 1 WHERE code = :code AND version = :version")
                .setParameter("title", currentTitle + " (edited)")
                .setParameter("code", boardCode)
                .setParameter("version", currentVersion)  // old version!
                .executeUpdate();

        log.info("  Step 3: User B tries to edit title with version={} -> {} rows affected (0 = conflict!)",
                currentVersion, editUpdateRows);

        // Step 4: Rollback the like change (restore original state)
        em.createNativeQuery(
                "UPDATE board SET like_cnt = :likeCnt, version = :version, title = :title WHERE code = :code")
                .setParameter("likeCnt", currentLikeCnt)
                .setParameter("version", currentVersion)
                .setParameter("title", currentTitle)
                .setParameter("code", boardCode)
                .executeUpdate();
        em.flush();

        log.info("  Step 4: Restored original state (version={}, like_cnt={})", currentVersion, currentLikeCnt);

        // Assertions
        assertThat(likeUpdateRows)
                .as("Like update with correct version should succeed")
                .isEqualTo(1);

        assertThat(editUpdateRows)
                .as("Edit with stale version should fail (0 rows affected), demonstrating @Version conflict")
                .isEqualTo(0);

        log.info("");
        log.info("  [RESULT] Demonstrated that like_cnt UPDATE increments @Version,");
        log.info("  causing concurrent title edits to fail (0 rows affected = OptimisticLockingFailureException).");
        log.info("  On high-traffic posts, this means frequent edit failures due to like activity.");
        log.info("  Recommended: Use bulk UPDATE (bypassing @Version) or separate counter table.");
    }

    // =========================================================================
    // Phase 2: Reply Pagination Comparison
    // =========================================================================

    @Test
    @Order(4)
    @Transactional(readOnly = true)
    @DisplayName("Phase 2: Reply Pagination Before/After Comparison")
    void phase2_PaginationComparison() {
        if (testPostCode == null) {
            log.warn("Skipping test: No reply data available");
            return;
        }

        log.info("============================================================");
        log.info("=== Phase 2: Reply Pagination Before/After Comparison ===");
        log.info("============================================================");

        // Check current root reply count for the test post
        Long rootReplyCount = em.createQuery(
                "SELECT COUNT(r) FROM Reply r WHERE r.postCode = :postCode " +
                "AND r.parentReplyCode IS NULL AND r.delYn = false", Long.class)
                .setParameter("postCode", testPostCode)
                .getSingleResult();

        log.info("  Test post {} has {} root replies", testPostCode, rootReplyCount);

        int iterations = 5;

        // === BEFORE: Full load (old method) ===
        List<Long> durationsOld = new ArrayList<>();
        List<Long> queryCountsOld = new ArrayList<>();
        int oldRecordCount = 0;

        for (int i = 0; i < iterations; i++) {
            statistics.clear();
            long startOld = System.nanoTime();
            List<ReplyResponse> oldResult = replyService.getRepliesByPostCode(testPostCode);
            long durationOld = (System.nanoTime() - startOld) / 1_000_000;
            long queryCountOld = statistics.getQueryExecutionCount();
            durationsOld.add(durationOld);
            queryCountsOld.add(queryCountOld);
            if (i == 0) oldRecordCount = oldResult.size();
        }

        // === AFTER: Paginated (new method) ===
        List<Long> durationsNew = new ArrayList<>();
        List<Long> queryCountsNew = new ArrayList<>();
        int newRecordCount = 0;
        int totalPages = 0;

        for (int i = 0; i < iterations; i++) {
            statistics.clear();
            long startNew = System.nanoTime();
            PageResponseDto<ReplyResponse> newResult =
                    replyService.getRepliesByPostCodePaginated(testPostCode, 1, 20);
            long durationNew = (System.nanoTime() - startNew) / 1_000_000;
            long queryCountNew = statistics.getQueryExecutionCount();
            durationsNew.add(durationNew);
            queryCountsNew.add(queryCountNew);
            if (i == 0) {
                newRecordCount = newResult.getContent().size();
                totalPages = newResult.getTotalPages();
            }
        }

        double avgOld = durationsOld.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgNew = durationsNew.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgQueriesOld = queryCountsOld.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgQueriesNew = queryCountsNew.stream().mapToLong(Long::longValue).average().orElse(0);
        double timeImprovement = avgOld > 0 ? (1.0 - avgNew / avgOld) * 100.0 : 0;

        log.info("--- Before (Full Load, {}x avg) ---", iterations);
        log.info("  Average duration: {}ms", String.format("%.1f", avgOld));
        log.info("  Average queries: {}", String.format("%.1f", avgQueriesOld));
        log.info("  Records returned: {} (all root replies)", oldRecordCount);

        log.info("--- After (Paginated, {}x avg) ---", iterations);
        log.info("  Average duration: {}ms", String.format("%.1f", avgNew));
        log.info("  Average queries: {}", String.format("%.1f", avgQueriesNew));
        log.info("  Records returned: {} root replies (page 1 of {})", newRecordCount, totalPages);

        log.info("--- Improvement ---");
        log.info("  Duration: {}ms -> {}ms ({}%)",
                String.format("%.1f", avgOld),
                String.format("%.1f", avgNew),
                String.format("%.1f", timeImprovement));
        log.info("  Data transfer: {} -> {} root replies per request", oldRecordCount, newRecordCount);

        if (rootReplyCount <= 20) {
            log.info("");
            log.info("  [NOTE] Test post has only {} root replies (fits in 1 page of 20).", rootReplyCount);
            log.info("  With small data, pagination adds overhead (count query + depth queries)");
            log.info("  without reducing data transfer. This is expected behavior.");
            log.info("  Pagination benefit is realized when root replies >> page size (e.g., 200+).");
            log.info("");
            log.info("  [EXPECTED BENEFIT AT SCALE]");
            log.info("  Root Replies | Full Load Returns | Paginated Returns | Transfer Reduction");
            log.info("  12           | 12                | 12                | 0% (no benefit)");
            log.info("  200          | 200               | 20                | 90%");
            log.info("  1,000        | 1,000             | 20                | 98%");
            log.info("  10,000       | 10,000            | 20                | 99.8%");
        }

        // Verify pagination returns correct page info
        if (rootReplyCount > 0) {
            assertThat(totalPages)
                    .as("Total pages should be correctly calculated")
                    .isGreaterThanOrEqualTo(1);
        }
    }

    // =========================================================================
    // Phase 2-2: Pagination with Large Data Simulation
    // =========================================================================

    @Test
    @Order(5)
    @Transactional
    @DisplayName("Phase 2-2: Pagination Large Data Simulation (200 root replies)")
    void phase2_PaginationLargeDataSimulation() {
        log.info("============================================================");
        log.info("=== Phase 2-2: Pagination Large Data (200 root replies) ===");
        log.info("============================================================");

        // Find a post code to use - pick the test one or generate a unique one
        String largeTestPostCode = "PERF_TEST_" + System.currentTimeMillis();

        // Find a valid member_id
        @SuppressWarnings("unchecked")
        List<Object> memberIds = em.createNativeQuery(
                "SELECT member_id FROM member LIMIT 1").getResultList();
        if (memberIds.isEmpty()) {
            log.warn("Skipping test: No member data available");
            return;
        }
        int memberId = ((Number) memberIds.get(0)).intValue();

        // Insert 200 root replies for the test post
        int rootReplyCount = 200;
        log.info("  Inserting {} root replies for post {}...", rootReplyCount, largeTestPostCode);

        for (int i = 0; i < rootReplyCount; i++) {
            em.createNativeQuery(
                    "INSERT INTO reply (reply_code, post_code, member_id, content, del_yn, like_cnt, reg_time, update_time, version) " +
                    "VALUES (:code, :postCode, :memberId, :content, false, 0, NOW(), NOW(), 0)")
                    .setParameter("code", "PERF_REP_" + System.currentTimeMillis() + "_" + i)
                    .setParameter("postCode", largeTestPostCode)
                    .setParameter("memberId", memberId)
                    .setParameter("content", "Performance test reply #" + i)
                    .executeUpdate();
        }
        em.flush();
        em.clear();

        log.info("  Inserted {} root replies.", rootReplyCount);

        // === BEFORE: Full load ===
        int fullLoadIterations = 5;
        List<Long> durationsOld = new ArrayList<>();
        int oldCount = 0;

        for (int i = 0; i < fullLoadIterations; i++) {
            statistics.clear();
            long start = System.nanoTime();
            List<ReplyResponse> result = replyService.getRepliesByPostCode(largeTestPostCode);
            long duration = (System.nanoTime() - start) / 1_000_000;
            durationsOld.add(duration);
            if (i == 0) oldCount = result.size();
        }

        // === AFTER: Paginated (page 1, 20 per page) ===
        List<Long> durationsNew = new ArrayList<>();
        int newCount = 0;
        int totalPages = 0;

        for (int i = 0; i < fullLoadIterations; i++) {
            statistics.clear();
            long start = System.nanoTime();
            PageResponseDto<ReplyResponse> result =
                    replyService.getRepliesByPostCodePaginated(largeTestPostCode, 1, 20);
            long duration = (System.nanoTime() - start) / 1_000_000;
            durationsNew.add(duration);
            if (i == 0) {
                newCount = result.getContent().size();
                totalPages = result.getTotalPages();
            }
        }

        double avgOld = durationsOld.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgNew = durationsNew.stream().mapToLong(Long::longValue).average().orElse(0);
        double timeImprovement = avgOld > 0 ? (1.0 - avgNew / avgOld) * 100.0 : 0;
        double dataReduction = oldCount > 0 ? (1.0 - (double) newCount / oldCount) * 100.0 : 0;

        log.info("--- Before (Full Load, {}x avg) ---", fullLoadIterations);
        log.info("  Average duration: {}ms", String.format("%.1f", avgOld));
        log.info("  Records returned: {} root replies", oldCount);

        log.info("--- After (Paginated, {}x avg) ---", fullLoadIterations);
        log.info("  Average duration: {}ms", String.format("%.1f", avgNew));
        log.info("  Records returned: {} root replies (page 1 of {})", newCount, totalPages);

        log.info("--- Improvement ---");
        log.info("  Duration: {}ms -> {}ms ({}%)",
                String.format("%.1f", avgOld),
                String.format("%.1f", avgNew),
                String.format("%.1f", timeImprovement));
        log.info("  Data transfer reduction: {} -> {} root replies ({}% reduction)",
                oldCount, newCount, String.format("%.1f", dataReduction));

        // With 200 root replies, pagination should return significantly fewer records
        assertThat(newCount)
                .as("Paginated result should return <= 20 root replies")
                .isLessThanOrEqualTo(20);

        assertThat(oldCount)
                .as("Full load should return all 200 root replies")
                .isGreaterThanOrEqualTo(rootReplyCount);

        assertThat(dataReduction)
                .as("Data transfer should be reduced by at least 80% with 200 root replies")
                .isGreaterThan(80.0);

        // Transaction will rollback, cleaning up test data automatically
        log.info("  (Test data will be rolled back by @Transactional)");
    }

    // =========================================================================
    // Phase 3: Index Comparison (DROP -> measure -> CREATE -> measure)
    // =========================================================================

    @Test
    @Order(6)
    @Transactional
    @DisplayName("Phase 3: Index Before/After Comparison (DROP/CREATE)")
    void phase3_IndexComparison() {
        log.info("============================================================");
        log.info("=== Phase 3: Index Before/After Comparison ===");
        log.info("============================================================");

        String[] indexes = {
            "idx_board_category_del_regtime",
            "idx_board_member_del",
            "idx_board_code_category_del",
            "idx_reply_postcode_del_regtime",
            "idx_reply_parent_del",
            "idx_reply_member_del",
            "idx_category_active_del",
            "idx_category_parent",
            "idx_likes_code",
            "idx_likes_member_code",
            "idx_bookreview_category_del_regtime",
            "idx_bookreview_member_del",
            "idx_bookreview_isbn"
        };
        String[] tables = {
            "board", "board", "board",
            "reply", "reply", "reply",
            "category", "category",
            "likes", "likes",
            "book_review", "book_review", "book_review"
        };

        // Store EXPLAIN results
        Map<String, String> explainBefore = new LinkedHashMap<>();
        Map<String, String> explainAfter = new LinkedHashMap<>();
        Map<String, Long> durationBefore = new LinkedHashMap<>();
        Map<String, Long> durationAfter = new LinkedHashMap<>();

        // Test queries
        String boardQuery = "SELECT * FROM board WHERE category_id = " + testCategoryId +
                " AND del_yn = false ORDER BY reg_time DESC LIMIT 20";
        String replyQuery = "SELECT * FROM reply WHERE post_code = '" + testPostCode +
                "' AND del_yn = false ORDER BY reg_time ASC LIMIT 50";
        String bookReviewQuery = "SELECT * FROM book_review WHERE category_id = " + testCategoryId +
                " AND del_yn = false ORDER BY reg_time DESC LIMIT 20";

        // -------------------------------------------------------------------
        // Step 1: Disable FK checks and DROP all indexes
        // -------------------------------------------------------------------
        log.info("Step 1: Disabling FOREIGN_KEY_CHECKS and dropping all performance indexes...");
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        List<String> droppedIndexes = new ArrayList<>();
        List<String> failedDropIndexes = new ArrayList<>();

        for (int i = 0; i < indexes.length; i++) {
            try {
                em.createNativeQuery("DROP INDEX " + indexes[i] + " ON " + tables[i]).executeUpdate();
                droppedIndexes.add(indexes[i]);
                log.info("  Dropped: {} on {}", indexes[i], tables[i]);
            } catch (Exception e) {
                failedDropIndexes.add(indexes[i]);
                log.info("  Failed to drop: {} on {} ({})", indexes[i], tables[i], e.getMessage());
            }
        }
        em.flush();

        log.info("  Dropped {}/{} indexes. Failed: {}", droppedIndexes.size(), indexes.length,
                failedDropIndexes.isEmpty() ? "none" : failedDropIndexes);

        // -------------------------------------------------------------------
        // Step 2: Warmup queries (discard first 2 runs to reduce cold-cache bias)
        // -------------------------------------------------------------------
        log.info("Step 2: Running warmup queries (2 iterations, discarded)...");
        measureNativeQuery(boardQuery, 2);
        measureNativeQuery(replyQuery, 2);
        measureNativeQuery(bookReviewQuery, 2);

        // -------------------------------------------------------------------
        // Step 3: Measure WITHOUT indexes (10 iterations)
        // -------------------------------------------------------------------
        log.info("Step 3: Measuring query performance WITHOUT indexes...");
        int measureIterations = 10;

        List<Long> boardDurationsBefore = measureNativeQuery(boardQuery, measureIterations);
        explainBefore.put("Board", executeExplain("EXPLAIN " + boardQuery));

        List<Long> replyDurationsBefore = measureNativeQuery(replyQuery, measureIterations);
        explainBefore.put("Reply", executeExplain("EXPLAIN " + replyQuery));

        List<Long> brDurationsBefore = measureNativeQuery(bookReviewQuery, measureIterations);
        explainBefore.put("BookReview", executeExplain("EXPLAIN " + bookReviewQuery));

        durationBefore.put("Board", Math.round(boardDurationsBefore.stream().mapToLong(Long::longValue).average().orElse(0)));
        durationBefore.put("Reply", Math.round(replyDurationsBefore.stream().mapToLong(Long::longValue).average().orElse(0)));
        durationBefore.put("BookReview", Math.round(brDurationsBefore.stream().mapToLong(Long::longValue).average().orElse(0)));

        // -------------------------------------------------------------------
        // Step 4: Re-CREATE all indexes
        // -------------------------------------------------------------------
        log.info("Step 4: Re-creating all performance indexes...");
        String[] createStatements = {
            "CREATE INDEX idx_board_category_del_regtime ON board (category_id, del_yn, reg_time DESC)",
            "CREATE INDEX idx_board_member_del ON board (member_id, del_yn)",
            "CREATE INDEX idx_board_code_category_del ON board (code, category_id, del_yn)",
            "CREATE INDEX idx_reply_postcode_del_regtime ON reply (post_code, del_yn, reg_time ASC)",
            "CREATE INDEX idx_reply_parent_del ON reply (parent_reply_code, del_yn)",
            "CREATE INDEX idx_reply_member_del ON reply (member_id, del_yn)",
            "CREATE INDEX idx_category_active_del ON category (is_active, del_yn)",
            "CREATE INDEX idx_category_parent ON category (p_category_id)",
            "CREATE INDEX idx_likes_code ON likes (code)",
            "CREATE INDEX idx_likes_member_code ON likes (member_id, code)",
            "CREATE INDEX idx_bookreview_category_del_regtime ON book_review (category_id, del_yn, reg_time DESC)",
            "CREATE INDEX idx_bookreview_member_del ON book_review (member_id, del_yn)",
            "CREATE INDEX idx_bookreview_isbn ON book_review (isbn)"
        };

        List<String> createdIndexes = new ArrayList<>();
        for (String createStmt : createStatements) {
            try {
                em.createNativeQuery(createStmt).executeUpdate();
                String idxName = createStmt.substring(createStmt.indexOf("idx_"), createStmt.indexOf(" ON"));
                createdIndexes.add(idxName);
                log.info("  Created: {}", idxName);
            } catch (Exception e) {
                log.info("  Skip (already exists or error): {}", e.getMessage());
            }
        }
        em.flush();

        // Re-enable FK checks
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

        // -------------------------------------------------------------------
        // Step 5: Warmup queries with indexes (discard first 2 runs)
        // -------------------------------------------------------------------
        log.info("Step 5: Running warmup queries WITH indexes (2 iterations, discarded)...");
        measureNativeQuery(boardQuery, 2);
        measureNativeQuery(replyQuery, 2);
        measureNativeQuery(bookReviewQuery, 2);

        // -------------------------------------------------------------------
        // Step 6: Measure WITH indexes (10 iterations)
        // -------------------------------------------------------------------
        log.info("Step 6: Measuring query performance WITH indexes...");

        List<Long> boardDurationsAfter = measureNativeQuery(boardQuery, measureIterations);
        explainAfter.put("Board", executeExplain("EXPLAIN " + boardQuery));

        List<Long> replyDurationsAfter = measureNativeQuery(replyQuery, measureIterations);
        explainAfter.put("Reply", executeExplain("EXPLAIN " + replyQuery));

        List<Long> brDurationsAfter = measureNativeQuery(bookReviewQuery, measureIterations);
        explainAfter.put("BookReview", executeExplain("EXPLAIN " + bookReviewQuery));

        durationAfter.put("Board", Math.round(boardDurationsAfter.stream().mapToLong(Long::longValue).average().orElse(0)));
        durationAfter.put("Reply", Math.round(replyDurationsAfter.stream().mapToLong(Long::longValue).average().orElse(0)));
        durationAfter.put("BookReview", Math.round(brDurationsAfter.stream().mapToLong(Long::longValue).average().orElse(0)));

        // -------------------------------------------------------------------
        // Step 7: Report results
        // -------------------------------------------------------------------
        log.info("============================================================");
        log.info("=== Phase 3 Results ===");
        log.info("============================================================");

        for (String entity : List.of("Board", "Reply", "BookReview")) {
            long before = durationBefore.getOrDefault(entity, 0L);
            long after = durationAfter.getOrDefault(entity, 0L);
            double improvement = before > 0 ? (1.0 - (double) after / before) * 100.0 : 0;

            log.info("--- {} Query ---", entity);
            log.info("  Without indexes: {}ms (avg of {}x)", before, measureIterations);
            log.info("  With indexes: {}ms (avg of {}x)", after, measureIterations);
            log.info("  Improvement: {}%", String.format("%.1f", improvement));
            log.info("  EXPLAIN Before: {}", explainBefore.get(entity));
            log.info("  EXPLAIN After: {}", explainAfter.get(entity));
        }

        log.info("");
        log.info("  [NOTE] FOREIGN_KEY_CHECKS=0 was used to DROP all {} indexes including FK-backed ones.", indexes.length);
        if (!failedDropIndexes.isEmpty()) {
            log.info("  {} indexes could not be dropped even with FK_CHECKS=0: {}",
                    failedDropIndexes.size(), failedDropIndexes);
        }
        log.info("  [CAVEAT] MySQL buffer pool caching may influence results.");
        log.info("  Warmup queries (2 iterations) were run before each measurement phase");
        log.info("  to reduce cold-cache bias, but residual caching effects may remain.");

        // Verify indexes exist again
        @SuppressWarnings("unchecked")
        List<Object[]> existingIndexes = em.createNativeQuery(
                "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND INDEX_NAME LIKE 'idx_%' " +
                "GROUP BY INDEX_NAME")
                .getResultList();

        log.info("--- Index Verification ---");
        log.info("  Total idx_* indexes found: {}", existingIndexes.size());
        assertThat(existingIndexes.size())
                .as("All 13 performance indexes should be restored")
                .isGreaterThanOrEqualTo(13);
    }

    // =========================================================================
    // Phase 4: Concurrency - ID Generation Comparison
    // =========================================================================

    @Test
    @Order(7)
    @DisplayName("Phase 4: ID Generation Before/After Comparison")
    void phase4_IdGenerationComparison() throws InterruptedException {
        log.info("============================================================");
        log.info("=== Phase 4: ID Generation Before/After Comparison ===");
        log.info("============================================================");

        int threadCount = 10;
        int idsPerThread = 1000;
        int totalIds = threadCount * idsPerThread;

        // === BEFORE: System.currentTimeMillis() pattern ===
        Set<String> oldIds = ConcurrentHashMap.newKeySet();
        AtomicInteger oldCollisions = new AtomicInteger(0);
        CountDownLatch oldStartLatch = new CountDownLatch(1);
        CountDownLatch oldDoneLatch = new CountDownLatch(threadCount);
        ExecutorService oldExecutor = Executors.newFixedThreadPool(threadCount);

        long oldStartTime = System.nanoTime();

        for (int t = 0; t < threadCount; t++) {
            oldExecutor.submit(() -> {
                try {
                    oldStartLatch.await();
                    for (int i = 0; i < idsPerThread; i++) {
                        String id = "BO_" + System.currentTimeMillis();
                        if (!oldIds.add(id)) {
                            oldCollisions.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    oldDoneLatch.countDown();
                }
            });
        }

        oldStartLatch.countDown();
        oldDoneLatch.await(60, TimeUnit.SECONDS);
        long oldDuration = (System.nanoTime() - oldStartTime) / 1_000_000;
        oldExecutor.shutdown();

        // === AFTER: DistributedIdGenerator ===
        Set<String> newIds = ConcurrentHashMap.newKeySet();
        AtomicInteger newCollisions = new AtomicInteger(0);
        CountDownLatch newStartLatch = new CountDownLatch(1);
        CountDownLatch newDoneLatch = new CountDownLatch(threadCount);
        ExecutorService newExecutor = Executors.newFixedThreadPool(threadCount);

        long newStartTime = System.nanoTime();

        for (int t = 0; t < threadCount; t++) {
            newExecutor.submit(() -> {
                try {
                    newStartLatch.await();
                    for (int i = 0; i < idsPerThread; i++) {
                        String id = idGenerator.generateBoardId();
                        if (!newIds.add(id)) {
                            newCollisions.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    newDoneLatch.countDown();
                }
            });
        }

        newStartLatch.countDown();
        newDoneLatch.await(60, TimeUnit.SECONDS);
        long newDuration = (System.nanoTime() - newStartTime) / 1_000_000;
        newExecutor.shutdown();

        // Calculate throughput
        double oldThroughput = oldDuration > 0 ? (double) totalIds / oldDuration * 1000.0 : 0;
        double newThroughput = newDuration > 0 ? (double) totalIds / newDuration * 1000.0 : 0;

        // Results
        double oldCollisionRate = (double) oldCollisions.get() / totalIds * 100.0;
        double newCollisionRate = (double) newCollisions.get() / totalIds * 100.0;

        log.info("--- Before (System.currentTimeMillis()) ---");
        log.info("  Threads: {}, IDs per thread: {}", threadCount, idsPerThread);
        log.info("  Total attempted: {}", totalIds);
        log.info("  Unique IDs: {}", oldIds.size());
        log.info("  Collisions: {} ({}%)", oldCollisions.get(), String.format("%.2f", oldCollisionRate));
        log.info("  Duration: {}ms", oldDuration);
        log.info("  Throughput: {} IDs/sec", String.format("%.0f", oldThroughput));

        log.info("--- After (DistributedIdGenerator / Snowflake) ---");
        log.info("  Threads: {}, IDs per thread: {}", threadCount, idsPerThread);
        log.info("  Total attempted: {}", totalIds);
        log.info("  Unique IDs: {}", newIds.size());
        log.info("  Collisions: {} ({}%)", newCollisions.get(), String.format("%.2f", newCollisionRate));
        log.info("  Duration: {}ms", newDuration);
        log.info("  Throughput: {} IDs/sec", String.format("%.0f", newThroughput));

        log.info("--- Improvement ---");
        log.info("  Collision reduction: {} -> {} ({}% -> {}%)",
                oldCollisions.get(), newCollisions.get(),
                String.format("%.2f", oldCollisionRate),
                String.format("%.2f", newCollisionRate));
        log.info("  Unique ID rate: {}% -> 100%",
                String.format("%.2f", (double) oldIds.size() / totalIds * 100.0));
        log.info("  Throughput: {} -> {} IDs/sec",
                String.format("%.0f", oldThroughput),
                String.format("%.0f", newThroughput));

        log.info("");
        log.info("  [NOTE] synchronized keyword in DistributedIdGenerator serializes all threads.");
        log.info("  Current throughput: {} IDs/sec (sufficient for typical web traffic).", String.format("%.0f", newThroughput));
        log.info("  At >10,000 req/sec, synchronized may become a bottleneck.");
        log.info("  Alternatives: AtomicLong-based CAS, ThreadLocal counters, or LongAdder.");
        log.info("");
        log.info("  [LIMITATION] This is an in-memory ID generation comparison.");
        log.info("  Actual DB-level concurrency (PK collisions, unique constraint violations,");
        log.info("  transaction rollbacks) requires separate integration testing with real INSERTs.");
        log.info("");
        log.info("  [LIMITATION] Single instance test (workerId=0).");
        log.info("  Multi-instance uniqueness is guaranteed by Snowflake's workerId separation");
        log.info("  but has not been tested in this suite.");

        // DistributedIdGenerator should have ZERO collisions
        assertThat(newCollisions.get())
                .as("DistributedIdGenerator should produce zero collisions")
                .isEqualTo(0);

        assertThat(newIds)
                .as("All generated IDs should be unique")
                .hasSize(totalIds);

        // Old pattern should have collisions (demonstrating the problem)
        log.info("  Old pattern collision count: {} (demonstrates the problem)", oldCollisions.get());
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private List<Long> measureNativeQuery(String query, int iterations) {
        List<Long> durations = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            try {
                em.createNativeQuery(query).getResultList();
            } catch (Exception e) {
                log.warn("Query failed: {}", e.getMessage());
            }
            long duration = (System.nanoTime() - start) / 1_000_000;
            durations.add(duration);
        }
        return durations;
    }

    private String executeExplain(String explainQuery) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> results = em.createNativeQuery(explainQuery).getResultList();
            StringBuilder sb = new StringBuilder();
            for (Object row : results) {
                if (row instanceof Object[] cols) {
                    // EXPLAIN columns: id, select_type, table, partitions, type, possible_keys, key, key_len, ref, rows, filtered, Extra
                    if (cols.length >= 12) {
                        sb.append("type=").append(cols[4])
                          .append(", key=").append(cols[6])
                          .append(", rows=").append(cols[9])
                          .append(", Extra=").append(cols[11]);
                    } else {
                        for (int i = 0; i < cols.length; i++) {
                            if (i > 0) sb.append(" | ");
                            sb.append(cols[i] != null ? cols[i].toString() : "NULL");
                        }
                    }
                } else {
                    sb.append(row != null ? row.toString() : "NULL");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
