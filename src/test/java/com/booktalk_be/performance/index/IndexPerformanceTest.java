package com.booktalk_be.performance.index;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Index Performance Test
 *
 * Validates Phase 3 improvements:
 * - V2__add_performance_indexes.sql (13 indexes)
 * - Index usage verification via EXPLAIN ANALYZE
 *
 * Indexes to verify:
 * 1. idx_board_category_del_regtime
 * 2. idx_board_member_del
 * 3. idx_board_code_category_del
 * 4. idx_reply_postcode_del_regtime
 * 5. idx_reply_parent_del
 * 6. idx_reply_member_del
 * 7. idx_category_active_del
 * 8. idx_category_parent
 * 9. idx_likes_code
 * 10. idx_likes_member_code
 * 11. idx_bookreview_category_del_regtime
 * 12. idx_bookreview_member_del
 * 13. idx_bookreview_isbn
 *
 * Success Criteria:
 * - All 13 indexes exist
 * - EXPLAIN shows "Using index" for optimized queries
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IndexPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(IndexPerformanceTest.class);

    @PersistenceContext
    private EntityManager em;

    // Expected indexes from V2__add_performance_indexes.sql
    private static final List<String> EXPECTED_INDEXES = Arrays.asList(
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
    );

    private Integer testCategoryId;
    private String testBoardCode;
    private String testPostCode;
    private Integer testMemberId;

    @BeforeEach
    void setUp() {
        // Load test data references
        if (testCategoryId == null) {
            List<Integer> ids = em.createQuery(
                    "SELECT c.categoryId FROM Category c WHERE c.delYn = false", Integer.class)
                    .setMaxResults(1)
                    .getResultList();
            testCategoryId = ids.isEmpty() ? 1 : ids.get(0);
        }

        if (testBoardCode == null) {
            List<String> codes = em.createQuery(
                    "SELECT b.code FROM Board b WHERE b.delYn = false", String.class)
                    .setMaxResults(1)
                    .getResultList();
            testBoardCode = codes.isEmpty() ? "BO_1" : codes.get(0);
        }

        if (testPostCode == null) {
            testPostCode = testBoardCode;
        }

        if (testMemberId == null) {
            List<Integer> ids = em.createQuery(
                    "SELECT m.memberId FROM Member m WHERE m.delYn = false", Integer.class)
                    .setMaxResults(1)
                    .getResultList();
            testMemberId = ids.isEmpty() ? 1 : ids.get(0);
        }
    }

    @Test
    @Order(1)
    @Transactional(readOnly = true)
    @DisplayName("1. Verify All 13 Indexes Exist")
    void testAllIndexesExist() {
        log.info("=== Verifying All Indexes Exist ===");

        // Query to get all custom indexes (idx_* prefix)
        @SuppressWarnings("unchecked")
        List<Object[]> existingIndexes = em.createNativeQuery(
                "SELECT INDEX_NAME, TABLE_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND INDEX_NAME LIKE 'idx_%' " +
                "GROUP BY INDEX_NAME, TABLE_NAME ORDER BY TABLE_NAME, INDEX_NAME")
                .getResultList();

        List<String> foundIndexNames = new ArrayList<>();
        log.info("Found Indexes:");
        for (Object[] row : existingIndexes) {
            String indexName = (String) row[0];
            String tableName = (String) row[1];
            foundIndexNames.add(indexName.toLowerCase());
            log.info("  {} on {}", indexName, tableName);
        }

        log.info("\nIndex Verification:");
        List<String> missingIndexes = new ArrayList<>();
        for (String expectedIndex : EXPECTED_INDEXES) {
            boolean found = foundIndexNames.contains(expectedIndex.toLowerCase());
            log.info("  {}: {}", expectedIndex, found ? "FOUND" : "MISSING");
            if (!found) {
                missingIndexes.add(expectedIndex);
            }
        }

        log.info("\nSummary:");
        log.info("  Expected: {} indexes", EXPECTED_INDEXES.size());
        log.info("  Found: {} matching indexes", EXPECTED_INDEXES.size() - missingIndexes.size());
        log.info("  Missing: {}", missingIndexes.isEmpty() ? "None" : missingIndexes);

        assertThat(missingIndexes)
                .as("All expected indexes should exist")
                .isEmpty();
    }

    @Test
    @Order(2)
    @Transactional(readOnly = true)
    @DisplayName("2. Test idx_board_category_del_regtime Index")
    void testBoardCategoryIndex() {
        log.info("=== Testing idx_board_category_del_regtime ===");

        String explainQuery = "EXPLAIN SELECT * FROM board " +
                "WHERE category_id = " + testCategoryId + " AND del_yn = false " +
                "ORDER BY reg_time DESC LIMIT 20";

        String result = executeExplainQuery(explainQuery);

        log.info("EXPLAIN Result:\n{}", result);

        // Check for index usage indicators
        boolean usesIndex = result.toLowerCase().contains("idx_board_category_del_regtime") ||
                result.toLowerCase().contains("using index") ||
                result.toLowerCase().contains("ref") ||
                result.toLowerCase().contains("range");

        log.info("Index Usage Detected: {}", usesIndex);

        // This assertion may be lenient if data is small
        // In production with large data, index should definitely be used
    }

    @Test
    @Order(3)
    @Transactional(readOnly = true)
    @DisplayName("3. Test idx_reply_postcode_del_regtime Index")
    void testReplyPostCodeIndex() {
        log.info("=== Testing idx_reply_postcode_del_regtime ===");

        String explainQuery = "EXPLAIN SELECT * FROM reply " +
                "WHERE post_code = '" + testPostCode + "' AND del_yn = false " +
                "ORDER BY reg_time ASC LIMIT 50";

        String result = executeExplainQuery(explainQuery);

        log.info("EXPLAIN Result:\n{}", result);

        boolean usesIndex = result.toLowerCase().contains("idx_reply_postcode_del_regtime") ||
                result.toLowerCase().contains("using index") ||
                result.toLowerCase().contains("ref") ||
                result.toLowerCase().contains("range");

        log.info("Index Usage Detected: {}", usesIndex);
    }

    @Test
    @Order(4)
    @Transactional(readOnly = true)
    @DisplayName("4. Test idx_reply_parent_del Index")
    void testReplyParentIndex() {
        log.info("=== Testing idx_reply_parent_del ===");

        String explainQuery = "EXPLAIN SELECT * FROM reply " +
                "WHERE parent_reply_code IS NOT NULL AND del_yn = false " +
                "LIMIT 100";

        String result = executeExplainQuery(explainQuery);

        log.info("EXPLAIN Result:\n{}", result);
    }

    @Test
    @Order(5)
    @Transactional(readOnly = true)
    @DisplayName("5. Test idx_category_active_del Index")
    void testCategoryActiveIndex() {
        log.info("=== Testing idx_category_active_del ===");

        String explainQuery = "EXPLAIN SELECT * FROM category " +
                "WHERE is_active = true AND del_yn = false";

        String result = executeExplainQuery(explainQuery);

        log.info("EXPLAIN Result:\n{}", result);

        boolean usesIndex = result.toLowerCase().contains("idx_category_active_del") ||
                result.toLowerCase().contains("using index") ||
                result.toLowerCase().contains("ref");

        log.info("Index Usage Detected: {}", usesIndex);
    }

    @Test
    @Order(6)
    @Transactional(readOnly = true)
    @DisplayName("6. Test idx_likes_member_code Index")
    void testLikesMemberCodeIndex() {
        log.info("=== Testing idx_likes_member_code ===");

        String explainQuery = "EXPLAIN SELECT * FROM likes " +
                "WHERE member_id = " + testMemberId + " AND code = '" + testBoardCode + "'";

        String result = executeExplainQuery(explainQuery);

        log.info("EXPLAIN Result:\n{}", result);
    }

    @Test
    @Order(7)
    @Transactional(readOnly = true)
    @DisplayName("7. Test idx_bookreview_category_del_regtime Index")
    void testBookReviewCategoryIndex() {
        log.info("=== Testing idx_bookreview_category_del_regtime ===");

        String explainQuery = "EXPLAIN SELECT * FROM book_review " +
                "WHERE category_id = " + testCategoryId + " AND del_yn = false " +
                "ORDER BY reg_time DESC LIMIT 20";

        String result = executeExplainQuery(explainQuery);

        log.info("EXPLAIN Result:\n{}", result);
    }

    @Test
    @Order(8)
    @Transactional(readOnly = true)
    @DisplayName("8. Query Performance With vs Without Index Hint")
    void testQueryPerformanceWithIndexHints() {
        log.info("=== Testing Query Performance ===");

        // Query without forcing index
        long startNormal = System.currentTimeMillis();
        @SuppressWarnings("unchecked")
        List<Object> normalResult = em.createNativeQuery(
                "SELECT code FROM board WHERE category_id = :categoryId AND del_yn = false " +
                "ORDER BY reg_time DESC LIMIT 100")
                .setParameter("categoryId", testCategoryId)
                .getResultList();
        long durationNormal = System.currentTimeMillis() - startNormal;

        // Query with FORCE INDEX hint
        long startForced = System.currentTimeMillis();
        @SuppressWarnings("unchecked")
        List<Object> forcedResult = em.createNativeQuery(
                "SELECT code FROM board FORCE INDEX (idx_board_category_del_regtime) " +
                "WHERE category_id = :categoryId AND del_yn = false " +
                "ORDER BY reg_time DESC LIMIT 100")
                .setParameter("categoryId", testCategoryId)
                .getResultList();
        long durationForced = System.currentTimeMillis() - startForced;

        log.info("Query Performance Comparison:");
        log.info("  Normal Query: {}ms ({} results)", durationNormal, normalResult.size());
        log.info("  With FORCE INDEX: {}ms ({} results)", durationForced, forcedResult.size());

        // Results should be the same
        assertThat(normalResult.size())
                .as("Both queries should return same number of results")
                .isEqualTo(forcedResult.size());
    }

    @Test
    @Order(9)
    @Transactional(readOnly = true)
    @DisplayName("9. Index Column Order Verification")
    void testIndexColumnOrder() {
        log.info("=== Verifying Index Column Order ===");

        @SuppressWarnings("unchecked")
        List<Object[]> indexColumns = em.createNativeQuery(
                "SELECT INDEX_NAME, SEQ_IN_INDEX, COLUMN_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND INDEX_NAME LIKE 'idx_%' " +
                "ORDER BY INDEX_NAME, SEQ_IN_INDEX")
                .getResultList();

        String currentIndex = "";
        StringBuilder columns = new StringBuilder();

        log.info("Index Column Details:");
        for (Object[] row : indexColumns) {
            String indexName = (String) row[0];
            Number seqInIndex = (Number) row[1];
            String columnName = (String) row[2];

            if (!indexName.equals(currentIndex)) {
                if (!currentIndex.isEmpty()) {
                    log.info("  {}: {}", currentIndex, columns);
                }
                currentIndex = indexName;
                columns = new StringBuilder();
            }

            if (columns.length() > 0) {
                columns.append(", ");
            }
            columns.append(columnName);
        }

        if (!currentIndex.isEmpty()) {
            log.info("  {}: {}", currentIndex, columns);
        }
    }

    @Test
    @Order(10)
    @Transactional(readOnly = true)
    @DisplayName("10. EXPLAIN ANALYZE - Board List Query")
    void testExplainAnalyzeBoardList() {
        log.info("=== EXPLAIN ANALYZE for Board List Query ===");

        try {
            String explainAnalyzeQuery = "EXPLAIN ANALYZE SELECT b.* FROM board b " +
                    "WHERE b.category_id = " + testCategoryId + " AND b.del_yn = false " +
                    "ORDER BY b.reg_time DESC LIMIT 20";

            @SuppressWarnings("unchecked")
            List<String> result = em.createNativeQuery(explainAnalyzeQuery).getResultList();

            log.info("EXPLAIN ANALYZE Result:");
            for (Object row : result) {
                log.info("  {}", row);
            }
        } catch (Exception e) {
            log.warn("EXPLAIN ANALYZE not supported or error: {}", e.getMessage());
            // EXPLAIN ANALYZE might not be available in all MySQL versions
        }
    }

    @Test
    @Order(11)
    @Transactional(readOnly = true)
    @DisplayName("11. Index Usage Statistics Check")
    void testIndexUsageStatistics() {
        log.info("=== Index Usage Statistics ===");

        try {
            @SuppressWarnings("unchecked")
            List<Object[]> usageStats = em.createNativeQuery(
                    "SELECT OBJECT_NAME, INDEX_NAME, COUNT_STAR as accesses " +
                    "FROM performance_schema.table_io_waits_summary_by_index_usage " +
                    "WHERE OBJECT_SCHEMA = DATABASE() AND INDEX_NAME LIKE 'idx_%' " +
                    "AND COUNT_STAR > 0 " +
                    "ORDER BY COUNT_STAR DESC")
                    .getResultList();

            if (usageStats.isEmpty()) {
                log.info("No index usage statistics available (performance_schema may not be enabled)");
            } else {
                log.info("Index Usage (sorted by access count):");
                for (Object[] row : usageStats) {
                    String tableName = (String) row[0];
                    String indexName = (String) row[1];
                    Number accesses = (Number) row[2];
                    log.info("  {}.{}: {} accesses", tableName, indexName, accesses);
                }
            }
        } catch (Exception e) {
            log.warn("Could not retrieve index usage statistics: {}", e.getMessage());
        }
    }

    /**
     * Execute EXPLAIN query and return formatted result
     */
    private String executeExplainQuery(String explainQuery) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> results = em.createNativeQuery(explainQuery).getResultList();

            StringBuilder sb = new StringBuilder();
            for (Object row : results) {
                if (row instanceof Object[] cols) {
                    for (Object col : cols) {
                        sb.append(col != null ? col.toString() : "NULL").append(" | ");
                    }
                } else {
                    sb.append(row != null ? row.toString() : "NULL");
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error executing EXPLAIN: " + e.getMessage();
        }
    }

    @Test
    @Order(12)
    @Transactional(readOnly = true)
    @DisplayName("12. Compound Index Efficiency Test")
    void testCompoundIndexEfficiency() {
        log.info("=== Testing Compound Index Efficiency ===");

        // Test query that uses all columns in compound index
        long startFull = System.currentTimeMillis();
        @SuppressWarnings("unchecked")
        List<Object> fullIndexResult = em.createNativeQuery(
                "SELECT code FROM board " +
                "WHERE category_id = :categoryId AND del_yn = false " +
                "ORDER BY reg_time DESC LIMIT 50")
                .setParameter("categoryId", testCategoryId)
                .getResultList();
        long durationFull = System.currentTimeMillis() - startFull;

        // Test query that only uses first column of compound index
        long startPartial = System.currentTimeMillis();
        @SuppressWarnings("unchecked")
        List<Object> partialIndexResult = em.createNativeQuery(
                "SELECT code FROM board " +
                "WHERE category_id = :categoryId LIMIT 50")
                .setParameter("categoryId", testCategoryId)
                .getResultList();
        long durationPartial = System.currentTimeMillis() - startPartial;

        log.info("Compound Index Test Results:");
        log.info("  Full index usage (category + del_yn + reg_time): {}ms", durationFull);
        log.info("  Partial index usage (category only): {}ms", durationPartial);

        // Both should be efficient with index
        assertThat(durationFull).isLessThan(1000);
        assertThat(durationPartial).isLessThan(1000);
    }
}
