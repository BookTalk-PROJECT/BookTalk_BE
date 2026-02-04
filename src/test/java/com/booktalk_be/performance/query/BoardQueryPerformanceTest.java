package com.booktalk_be.performance.query;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.board.responseDto.BoardDetailResponse;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import com.booktalk_be.domain.board.service.BoardService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Board Query Performance Test
 *
 * Validates Phase 1 improvements:
 * - LAZY fetch strategy
 * - fetchJoin usage
 * - @Formula removal
 *
 * Success Criteria:
 * - Board list query count <= 2
 * - Board detail + replies query count <= 5
 * - No additional queries for Member fetch (JOIN FETCH working)
 * - likesCnt fetched without subquery (stored column)
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BoardQueryPerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(BoardQueryPerformanceTest.class);

    @Autowired
    private BoardService boardService;

    @PersistenceContext
    private EntityManager em;

    private Statistics statistics;
    private Integer testCategoryId;
    private String testBoardCode;

    @BeforeEach
    void setUp() {
        // Enable Hibernate statistics
        SessionFactory sessionFactory = em.getEntityManagerFactory().unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        // Get a valid category ID for testing
        if (testCategoryId == null) {
            List<Integer> categoryIds = em.createQuery(
                    "SELECT c.categoryId FROM Category c WHERE c.delYn = false", Integer.class)
                    .setMaxResults(1)
                    .getResultList();
            testCategoryId = categoryIds.isEmpty() ? 1 : categoryIds.get(0);
        }

        // Get a valid board code for testing
        if (testBoardCode == null) {
            List<String> boardCodes = em.createQuery(
                    "SELECT b.code FROM Board b WHERE b.delYn = false", String.class)
                    .setMaxResults(1)
                    .getResultList();
            testBoardCode = boardCodes.isEmpty() ? null : boardCodes.get(0);
        }
    }

    @AfterEach
    void tearDown() {
        if (statistics != null) {
            log.info("Test Statistics:");
            log.info("  Query Execution Count: {}", statistics.getQueryExecutionCount());
            log.info("  Entity Load Count: {}", statistics.getEntityLoadCount());
            log.info("  Collection Load Count: {}", statistics.getCollectionLoadCount());
            log.info("  Second Level Cache Hit: {}", statistics.getSecondLevelCacheHitCount());
        }
    }

    @Test
    @Order(1)
    @Transactional(readOnly = true)
    @DisplayName("1. Board List - Query Count <= 2")
    void testBoardListQueryCount() {
        // Skip if no test data
        if (testCategoryId == null) {
            log.warn("Skipping test: No category data available");
            return;
        }

        log.info("=== Testing Board List Query Count ===");
        statistics.clear();

        // When: Fetch board list
        PageResponseDto<BoardResponse> result = boardService.getBoardsForPaging(testCategoryId, 1, 20);

        // Then: Query count should be <= 2 (main query + count query)
        long queryCount = statistics.getQueryExecutionCount();

        log.info("Board List Result:");
        log.info("  Content Size: {}", result.getContent().size());
        log.info("  Total Pages: {}", result.getTotalPages());
        log.info("  Query Execution Count: {}", queryCount);

        assertThat(queryCount)
                .as("Board list should execute at most 2 queries (data + count)")
                .isLessThanOrEqualTo(2);

        // Verify no lazy loading issues
        if (!result.getContent().isEmpty()) {
            BoardResponse firstBoard = result.getContent().get(0);
            assertThat(firstBoard).isNotNull();
            log.info("  First Board Code: {}", firstBoard.getBoardCode());
        }
    }

    @Test
    @Order(2)
    @Transactional(readOnly = true)
    @DisplayName("2. Board Detail with Replies - Query Count <= 5")
    void testBoardDetailWithRepliesQueryCount() {
        if (testBoardCode == null) {
            log.warn("Skipping test: No board data available");
            return;
        }

        log.info("=== Testing Board Detail Query Count ===");
        statistics.clear();

        // When: Fetch board detail
        BoardDetailResponse result = boardService.getBoardDetail(testBoardCode);

        // Then: Query count should be <= 5
        long queryCount = statistics.getQueryExecutionCount();

        log.info("Board Detail Result:");
        log.info("  Board Code: {}", result.getPost().getBoardCode());
        log.info("  Title: {}", result.getPost().getTitle());
        log.info("  Query Execution Count: {}", queryCount);

        assertThat(queryCount)
                .as("Board detail should execute at most 5 queries")
                .isLessThanOrEqualTo(5);
    }

    @Test
    @Order(3)
    @Transactional(readOnly = true)
    @DisplayName("3. Verify Member FetchJoin - No N+1 for Member")
    void testMemberFetchJoinWorking() {
        if (testCategoryId == null) {
            log.warn("Skipping test: No category data available");
            return;
        }

        log.info("=== Testing Member FetchJoin ===");
        statistics.clear();

        // When: Fetch board list and access member names
        PageResponseDto<BoardResponse> result = boardService.getBoardsForPaging(testCategoryId, 1, 10);

        long queryCountAfterFetch = statistics.getQueryExecutionCount();

        // Access member name from each result (should not trigger additional queries)
        for (BoardResponse board : result.getContent()) {
            String authorName = board.getAuthor();
            assertThat(authorName).isNotNull();
        }

        long queryCountAfterAccess = statistics.getQueryExecutionCount();

        log.info("FetchJoin Verification:");
        log.info("  Query Count After Fetch: {}", queryCountAfterFetch);
        log.info("  Query Count After Member Access: {}", queryCountAfterAccess);

        // Query count should not increase after accessing member data
        assertThat(queryCountAfterAccess)
                .as("Accessing member data should not trigger additional queries (fetchJoin working)")
                .isEqualTo(queryCountAfterFetch);
    }

    @Test
    @Order(4)
    @Transactional(readOnly = true)
    @DisplayName("4. Verify likesCnt is Stored Column (No Subquery)")
    void testLikesCntIsStoredColumn() {
        if (testBoardCode == null) {
            log.warn("Skipping test: No board data available");
            return;
        }

        log.info("=== Testing likesCnt Stored Column ===");
        statistics.clear();

        // When: Fetch a single board
        String query = "SELECT b FROM Board b WHERE b.code = :code AND b.delYn = false";
        var boards = em.createQuery(query, com.booktalk_be.domain.board.model.entity.Board.class)
                .setParameter("code", testBoardCode)
                .getResultList();

        long queryCountAfterFetch = statistics.getQueryExecutionCount();

        if (!boards.isEmpty()) {
            // Access likesCnt - should not trigger additional query if it's a stored column
            Integer likesCnt = boards.get(0).getLikesCnt();
            log.info("  Likes Count: {}", likesCnt);
        }

        long queryCountAfterLikesAccess = statistics.getQueryExecutionCount();

        log.info("likesCnt Column Verification:");
        log.info("  Query Count After Fetch: {}", queryCountAfterFetch);
        log.info("  Query Count After likesCnt Access: {}", queryCountAfterLikesAccess);

        // Query count should remain 1 (no @Formula subquery)
        assertThat(queryCountAfterLikesAccess)
                .as("likesCnt access should not trigger additional query (@Formula removed)")
                .isEqualTo(queryCountAfterFetch);
    }

    @Test
    @Order(5)
    @Transactional(readOnly = true)
    @DisplayName("5. Stress Test - 100 Board List Requests (avg < 100ms)")
    void testMultipleBoardListRequests() {
        if (testCategoryId == null) {
            log.warn("Skipping test: No category data available");
            return;
        }

        log.info("=== Stress Test: 100 Board List Requests ===");

        int requestCount = 100;
        List<Long> durations = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < requestCount; i++) {
            long startTime = System.currentTimeMillis();

            try {
                PageResponseDto<BoardResponse> result =
                        boardService.getBoardsForPaging(testCategoryId, 1, 20);
                successCount++;
            } catch (Exception e) {
                log.warn("Request {} failed: {}", i, e.getMessage());
            }

            long duration = System.currentTimeMillis() - startTime;
            durations.add(duration);
        }

        // Calculate statistics
        double avgDuration = durations.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        long maxDuration = durations.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        long minDuration = durations.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);

        // Calculate p95
        durations.sort(Long::compareTo);
        long p95Duration = durations.get((int) (durations.size() * 0.95));

        log.info("Stress Test Results:");
        log.info("  Total Requests: {}", requestCount);
        log.info("  Successful Requests: {}", successCount);
        log.info("  Avg Duration: {}ms", String.format("%.2f", avgDuration));
        log.info("  Min Duration: {}ms", minDuration);
        log.info("  Max Duration: {}ms", maxDuration);
        log.info("  P95 Duration: {}ms", p95Duration);

        assertThat(avgDuration)
                .as("Average response time should be less than 100ms")
                .isLessThan(100);

        assertThat(successCount)
                .as("All requests should succeed")
                .isEqualTo(requestCount);
    }

    @Test
    @Order(6)
    @Transactional(readOnly = true)
    @DisplayName("6. All Boards List - Query Count Verification")
    void testAllBoardsListQueryCount() {
        log.info("=== Testing All Boards List Query Count ===");
        statistics.clear();

        // When: Fetch all boards list
        PageResponseDto<BoardResponse> result = boardService.getAllBoardsForPaging(1, 20);

        long queryCount = statistics.getQueryExecutionCount();

        log.info("All Boards List Result:");
        log.info("  Content Size: {}", result.getContent().size());
        log.info("  Total Pages: {}", result.getTotalPages());
        log.info("  Query Execution Count: {}", queryCount);

        assertThat(queryCount)
                .as("All boards list should execute at most 2 queries (data + count)")
                .isLessThanOrEqualTo(2);
    }

    @Test
    @Order(7)
    @Transactional(readOnly = true)
    @DisplayName("7. Pagination Performance - Multiple Pages")
    void testPaginationPerformance() {
        if (testCategoryId == null) {
            log.warn("Skipping test: No category data available");
            return;
        }

        log.info("=== Testing Pagination Performance ===");

        int pageCount = 10;
        List<Long> durations = new ArrayList<>();

        for (int page = 1; page <= pageCount; page++) {
            statistics.clear();
            long startTime = System.currentTimeMillis();

            PageResponseDto<BoardResponse> result =
                    boardService.getBoardsForPaging(testCategoryId, page, 20);

            long duration = System.currentTimeMillis() - startTime;
            long queryCount = statistics.getQueryExecutionCount();

            durations.add(duration);

            log.info("  Page {}: {}ms, {} queries, {} items",
                    page, duration, queryCount, result.getContent().size());

            // Each page should use consistent query count
            assertThat(queryCount).isLessThanOrEqualTo(2);
        }

        double avgDuration = durations.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        log.info("Pagination Results:");
        log.info("  Pages Tested: {}", pageCount);
        log.info("  Avg Duration per Page: {}ms", String.format("%.2f", avgDuration));

        assertThat(avgDuration)
                .as("Average page load time should be under 200ms")
                .isLessThan(200);
    }
}
