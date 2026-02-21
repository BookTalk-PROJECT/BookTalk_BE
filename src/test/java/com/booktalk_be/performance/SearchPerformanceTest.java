package com.booktalk_be.performance;

import com.booktalk_be.domain.auth.model.entity.AuthenticateType;
import com.booktalk_be.domain.board.model.repository.BoardRepository;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import com.booktalk_be.domain.member.model.entity.Member;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 관리자 페이지 검색 성능 테스트
 *
 * containsIgnoreCase → MATCH AGAINST (FULLTEXT) 전환 전/후 성능 비교.
 * CrossJoinBugPerformanceTest와 동일한 패턴(MockMvc + warm-up 3회 + 측정 5회 + CSV 출력).
 *
 * 사전 조건: 로컬 MySQL(localhost:3306/booktalk)이 실행 중이어야 합니다.
 *
 * 테스트 케이스:
 *   1. POST /community/board/admin/search (title)      — 게시글 제목 검색
 *   2. POST /community/board/admin/search (author)     — 게시글 작성자 검색
 *   3. POST /community/board/admin/search (category)   — 게시글 카테고리 검색
 *   4. POST /community/board/admin/search (board_code) — 게시글 코드 검색
 *   5. POST /reply/admin/search?postType=community (content)   — 댓글 내용 검색 (BO_)
 *   6. POST /reply/admin/search?postType=bookreview (content)  — 댓글 내용 검색 (BR_)
 *   7. POST /reply/admin/search?postType=community (post_code) — 댓글 게시글코드 검색
 *   8. POST /reply/admin/search?postType=community (content+날짜) — 댓글 내용 + 날짜 필터
 */
@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SearchPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardRepository boardRepository;

    private static final int WARM_UP = 3;
    private static final int ITERATIONS = 5;

    private final List<String[]> csvRows = new ArrayList<>();

    /** DB에서 조회한 게시글 코드 (코드 검색 테스트용) */
    private String testBoardCode;

    // ─── Setup ───────────────────────────────────────────────

    @BeforeAll
    void lookupTestData() {
        System.out.println("\n══════════════════════════════════════════════════════════════");
        System.out.println("  Admin Search — Performance Test");
        System.out.println("══════════════════════════════════════════════════════════════");
        try {
            Page<BoardResponse> page = boardRepository.findBoardsForPaging(1, PageRequest.of(0, 1));
            if (!page.isEmpty()) {
                testBoardCode = page.getContent().get(0).getBoardCode();
                System.out.println("  Test board code found: " + testBoardCode);
            } else {
                System.out.println("  Warning: No board data in category 1");
            }
        } catch (Exception e) {
            System.err.println("  Warning: Could not query test data — " + e.getMessage());
        }
        System.out.println("──────────────────────────────────────────────────────────────\n");
    }

    // ─── Authentication Helpers ──────────────────────────────

    private Authentication adminAuth() {
        Member m = Member.builder()
                .email("perf-test@booktalk.com")
                .name("PerfTestUser")
                .authType(AuthenticateType.OWN)
                .password("test1234")
                .build();
        try {
            Field f = Member.class.getDeclaredField("memberId");
            f.setAccessible(true);
            f.setInt(m, 1);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return new UsernamePasswordAuthenticationToken(
                m, null, List.of(new SimpleGrantedAuthority("ADMIN")));
    }

    // ─── Performance Measurement ─────────────────────────────

    @FunctionalInterface
    interface TestAction {
        void run() throws Exception;
    }

    private void measure(String label, TestAction action) throws Exception {
        for (int i = 0; i < WARM_UP; i++) {
            try { action.run(); } catch (Exception ignored) {}
        }

        long[] ms = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long t0 = System.nanoTime();
            action.run();
            ms[i] = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
        }

        long sum = 0, min = Long.MAX_VALUE, max = 0;
        for (long t : ms) {
            sum += t;
            min = Math.min(min, t);
            max = Math.max(max, t);
        }
        long avg = sum / ms.length;

        System.out.printf("  %-65s  avg=%4dms  min=%4dms  max=%4dms  %s%n",
                label, avg, min, max, Arrays.toString(ms));
        csvRows.add(new String[]{label, String.valueOf(avg), String.valueOf(min), String.valueOf(max)});
    }

    // ═══════════════════════════════════════════════════════════
    //  Board Admin Search Tests
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("[SEARCH] POST /community/board/admin/search (TITLE) — 게시글 제목 검색")
    void boardSearchTitle() throws Exception {
        measure("POST /board/admin/search (TITLE, keyword=테스트)", () ->
                mockMvc.perform(post("/community/board/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"title\",\"keyword\":\"테스트\",\"startDate\":\"\",\"endDate\":\"\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk()));
    }

    @Test
    @Order(2)
    @DisplayName("[SEARCH] POST /community/board/admin/search (AUTHOR) — 게시글 작성자 검색")
    void boardSearchAuthor() throws Exception {
        measure("POST /board/admin/search (AUTHOR, keyword=test)", () ->
                mockMvc.perform(post("/community/board/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"author\",\"keyword\":\"test\",\"startDate\":\"\",\"endDate\":\"\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk()));
    }

    @Test
    @Order(3)
    @DisplayName("[SEARCH] POST /community/board/admin/search (CATEGORY) — 게시글 카테고리 검색")
    void boardSearchCategory() throws Exception {
        measure("POST /board/admin/search (CATEGORY, keyword=자유)", () ->
                mockMvc.perform(post("/community/board/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"category\",\"keyword\":\"자유\",\"startDate\":\"\",\"endDate\":\"\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk()));
    }

    @Test
    @Order(4)
    @DisplayName("[SEARCH] POST /community/board/admin/search (CODE) — 게시글 코드 검색")
    void boardSearchCode() throws Exception {
        Assumptions.assumeTrue(testBoardCode != null, "DB에 게시글 데이터 없음 → skip");
        measure("POST /board/admin/search (CODE, keyword=" + testBoardCode + ")", () ->
                mockMvc.perform(post("/community/board/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"board_code\",\"keyword\":\"" + testBoardCode + "\",\"startDate\":\"\",\"endDate\":\"\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk()));
    }

    // ═══════════════════════════════════════════════════════════
    //  Reply Admin Search Tests
    // ═══════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("[SEARCH] POST /reply/admin/search (CONTENT, community) — 댓글 내용 검색 (BO_)")
    void replySearchContentCommunity() throws Exception {
        measure("POST /reply/admin/search (CONTENT, community, keyword=테스트)", () ->
                mockMvc.perform(post("/reply/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .param("postType", "community")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"content\",\"keyword\":\"테스트\",\"startDate\":\"\",\"endDate\":\"\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk()));
    }

    @Test
    @Order(6)
    @DisplayName("[SEARCH] POST /reply/admin/search (CONTENT, bookreview) — 댓글 내용 검색 (BR_)")
    void replySearchContentBookreview() throws Exception {
        measure("POST /reply/admin/search (CONTENT, bookreview, keyword=테스트)", () ->
                mockMvc.perform(post("/reply/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .param("postType", "bookreview")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"content\",\"keyword\":\"테스트\",\"startDate\":\"\",\"endDate\":\"\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk()));
    }

    @Test
    @Order(7)
    @DisplayName("[SEARCH] POST /reply/admin/search (POST_CODE, community) — 댓글 게시글코드 검색")
    void replySearchPostCode() throws Exception {
        measure("POST /reply/admin/search (POST_CODE, community, keyword=BO_)", () ->
                mockMvc.perform(post("/reply/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .param("postType", "community")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"post_code\",\"keyword\":\"BO_\",\"startDate\":\"\",\"endDate\":\"\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk()));
    }

    @Test
    @Order(8)
    @DisplayName("[SEARCH] POST /reply/admin/search (CONTENT+DATE, community) — 댓글 내용 + 날짜 필터")
    void replySearchContentWithDate() throws Exception {
        measure("POST /reply/admin/search (CONTENT+DATE, community, keyword=테스트)", () ->
                mockMvc.perform(post("/reply/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .param("postType", "community")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"content\",\"keyword\":\"테스트\",\"startDate\":\"2024-01-01\",\"endDate\":\"2025-12-31\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk()));
    }

    // ═══════════════════════════════════════════════════════════
    //  Results Output
    // ═══════════════════════════════════════════════════════════

    @AfterAll
    void writeResults() throws Exception {
        String phase = System.getProperty("test.phase", "before");
        Path dir = Paths.get("src", "test", "resources", "performance");
        Files.createDirectories(dir);

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path csvFile = dir.resolve("search_results_" + phase + "_" + ts + ".csv");
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(csvFile))) {
            pw.println("endpoint,avg_ms,min_ms,max_ms");
            for (String[] row : csvRows) {
                pw.printf("%s,%s,%s,%s%n", row[0], row[1], row[2], row[3]);
            }
        }

        System.out.println("\n══════════════════════════════════════════════════════════════");
        System.out.printf("  Search Performance Results — Phase: %s%n", phase.toUpperCase());
        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.printf("  %-65s  %8s %8s %8s%n", "Endpoint", "Avg(ms)", "Min(ms)", "Max(ms)");
        System.out.println("  " + "─".repeat(95));
        for (String[] row : csvRows) {
            System.out.printf("  %-65s  %8s %8s %8s%n", row[0], row[1], row[2], row[3]);
        }
        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.println("  Results saved → " + csvFile.toAbsolutePath());
        System.out.println("══════════════════════════════════════════════════════════════");
    }
}
