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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * QueryDSL CROSS JOIN 버그 성능 통합 테스트
 *
 * 프론트엔드 호출 스펙과 동일한 API 엔드포인트를 MockMvc로 호출하여
 * 응답 정확성과 응답 시간을 측정합니다.
 *
 * 사전 조건: 로컬 MySQL(localhost:3306/booktalk)이 실행 중이어야 합니다.
 *
 * 테스트 대상 (CROSS JOIN 버그 영향 메서드):
 *   1. findBoardsForPaging       → GET  /community/board/list
 *   2. getBoardDetailBy          → GET  /community/board/detail/{code}
 *   3. getAllBoardsForPaging      → GET  /community/board/admin/all
 *   4. searchAllBoardsForPaging   → POST /community/board/admin/search
 *   5. getAllBoardsForPagingByMe   → GET  /community/board/mylist
 *   6. searchAllBoardsForPagingByMe → POST /community/board/mylist/search
 *
 * Count 쿼리 최적화 대상:
 *   7. getAllBoardsForPaging count      (불필요 member JOIN 제거)
 *   8. searchAllBoardsForPaging count   (AUTHOR 검색 시 member JOIN 필요)
 *   9. getAllRepliesForPaging count      (불필요 member JOIN 제거)
 */
@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CrossJoinBugPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardRepository boardRepository;

    /** warm-up 라운드 수 (JIT·캐시 안정화) */
    private static final int WARM_UP = 3;
    /** 실제 측정 라운드 수 */
    private static final int ITERATIONS = 5;

    /** 테스트별 [avg, min, max] 결과를 순서대로 저장 */
    private final List<String[]> csvRows = new ArrayList<>();

    /** DB에서 조회한 기존 게시글 코드 (상세 조회 테스트용) */
    private String testBoardCode;

    // ─── Setup ───────────────────────────────────────────────

    @BeforeAll
    void lookupTestData() {
        System.out.println("\n══════════════════════════════════════════════════════════════");
        System.out.println("  QueryDSL CROSS JOIN Bug — Performance Test");
        System.out.println("══════════════════════════════════════════════════════════════");
        try {
            Page<BoardResponse> page = boardRepository.findBoardsForPaging(1, PageRequest.of(0, 1));
            if (!page.isEmpty()) {
                testBoardCode = page.getContent().get(0).getBoardCode();
                System.out.println("  Test board code found: " + testBoardCode);
            } else {
                System.out.println("  Warning: No board data in category 1 — detail test will be skipped");
            }
        } catch (Exception e) {
            System.err.println("  Warning: Could not query test data — " + e.getMessage());
        }
        System.out.println("──────────────────────────────────────────────────────────────\n");
    }

    // ─── Authentication Helpers ──────────────────────────────

    private Authentication adminAuth() {
        return makeAuth(1, "ADMIN");
    }

    private Authentication userAuth() {
        return makeAuth(1, "COMMON");
    }

    private Authentication makeAuth(int memberId, String authority) {
        Member m = Member.builder()
                .email("perf-test@booktalk.com")
                .name("PerfTestUser")
                .authType(AuthenticateType.OWN)
                .password("test1234")
                .build();
        try {
            Field f = Member.class.getDeclaredField("memberId");
            f.setAccessible(true);
            f.setInt(m, memberId);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return new UsernamePasswordAuthenticationToken(
                m, null, List.of(new SimpleGrantedAuthority(authority)));
    }

    // ─── Performance Measurement ─────────────────────────────

    @FunctionalInterface
    interface TestAction {
        void run() throws Exception;
    }

    /**
     * warm-up 후 ITERATIONS 회 반복 측정하여 avg/min/max를 기록합니다.
     */
    private void measure(String label, TestAction action) throws Exception {
        // Warm-up: JIT 컴파일 및 DB 캐시 안정화
        for (int i = 0; i < WARM_UP; i++) {
            try { action.run(); } catch (Exception ignored) {}
        }

        // Measurement
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

        System.out.printf("  %-60s  avg=%4dms  min=%4dms  max=%4dms  %s%n",
                label, avg, min, max, Arrays.toString(ms));
        csvRows.add(new String[]{label, String.valueOf(avg), String.valueOf(min), String.valueOf(max)});
    }

    // ═══════════════════════════════════════════════════════════
    //  Board API Tests — 프론트엔드 호출 스펙 기준
    // ═══════════════════════════════════════════════════════════

    /**
     * 프론트엔드: boardApi.getBoards(categoryId, pageNum)
     * 영향받는 메서드: BoardRepositoryCustomImpl.findBoardsForPaging
     * 버그: .from(board) 중복 → CROSS JOIN
     */
    @Test
    @Order(1)
    @DisplayName("[CROSS JOIN] GET /community/board/list — 게시글 목록 조회")
    void boardList() throws Exception {
        measure("GET /community/board/list", () ->
                mockMvc.perform(get("/community/board/list")
                                .param("categoryId", "1")
                                .param("pageNum", "1")
                                .param("pageSize", "10"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200))
                        .andExpect(jsonPath("$.data.content").isArray()));
    }

    /**
     * 프론트엔드: boardApi.searchBoards(categoryId, pageNum, searchParams)
     * 영향받는 메서드: BoardRepositoryCustomImpl.searchBoardsForPaging
     * 이 메서드는 CROSS JOIN 버그 없음 — 성능 기준선으로 사용
     */
    @Test
    @Order(2)
    @DisplayName("[BASELINE] POST /community/board/list/search — 게시글 검색 (기준선)")
    void boardSearch() throws Exception {
        measure("POST /community/board/list/search", () ->
                mockMvc.perform(post("/community/board/list/search")
                                .param("categoryId", "1")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"title\",\"keyword\":\"\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));
    }

    /**
     * 프론트엔드: boardApi.getBoardDetail(postId)
     * 영향받는 메서드: BoardRepositoryCustomImpl.getBoardDetailBy
     * 버그: .from(board) 중복 → CROSS JOIN
     */
    @Test
    @Order(3)
    @DisplayName("[CROSS JOIN] GET /community/board/detail/{code} — 게시글 상세 조회")
    void boardDetail() throws Exception {
        Assumptions.assumeTrue(testBoardCode != null, "DB에 게시글 데이터 없음 → skip");
        measure("GET /community/board/detail/{code}", () ->
                mockMvc.perform(get("/community/board/detail/" + testBoardCode))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200))
                        .andExpect(jsonPath("$.data.post").exists()));
    }

    /**
     * 관리자 페이지: 게시글 전체 목록 조회
     * 영향받는 메서드: BoardRepositoryCustomImpl.getAllBoardsForPaging
     * 버그: .from(board) 중복 → CROSS JOIN + count 쿼리 불필요 JOIN
     */
    @Test
    @Order(4)
    @DisplayName("[CROSS JOIN] GET /community/board/admin/all — 관리자 게시글 목록")
    void adminBoardList() throws Exception {
        measure("GET /community/board/admin/all", () ->
                mockMvc.perform(get("/community/board/admin/all")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200))
                        .andExpect(jsonPath("$.data.content").isArray()));
    }

    /**
     * 관리자 페이지: 게시글 제목 검색
     * 영향받는 메서드: BoardRepositoryCustomImpl.searchAllBoardsForPaging
     * 버그: .from(board) 중복 → CROSS JOIN
     */
    @Test
    @Order(5)
    @DisplayName("[CROSS JOIN] POST /community/board/admin/search (TITLE) — 관리자 게시글 검색")
    void adminBoardSearchTitle() throws Exception {
        measure("POST /board/admin/search (TITLE)", () ->
                mockMvc.perform(post("/community/board/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"title\",\"keyword\":\"\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));
    }

    /**
     * 관리자 페이지: 작성자 검색 — count 쿼리에서 member JOIN 필요
     * 영향받는 메서드: BoardRepositoryCustomImpl.searchAllBoardsForPaging
     * 수정: count 쿼리에 leftJoin(board.member) 추가
     */
    @Test
    @Order(6)
    @DisplayName("[COUNT FIX] POST /community/board/admin/search (AUTHOR) — 작성자 검색")
    void adminBoardSearchAuthor() throws Exception {
        measure("POST /board/admin/search (AUTHOR)", () ->
                mockMvc.perform(post("/community/board/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"author\",\"keyword\":\"test\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk()));
    }

    /**
     * 관리자 페이지: 카테고리 검색 — subquery 방식이므로 category JOIN 불필요
     * 영향받는 메서드: BoardRepositoryCustomImpl.searchAllBoardsForPaging
     */
    @Test
    @Order(7)
    @DisplayName("[COUNT FIX] POST /community/board/admin/search (CATEGORY) — 카테고리 검색")
    void adminBoardSearchCategory() throws Exception {
        measure("POST /board/admin/search (CATEGORY)", () ->
                mockMvc.perform(post("/community/board/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"category\",\"keyword\":\"자유\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk()));
    }

    /**
     * 마이페이지: 내 게시글 목록
     * 영향받는 메서드: BoardRepositoryCustomImpl.getAllBoardsForPagingByMe
     * 버그: .from(board) 중복 + where 절 위치 오류 → CROSS JOIN
     */
    @Test
    @Order(8)
    @DisplayName("[CROSS JOIN] GET /community/board/mylist — 내 게시글 목록")
    void myBoardList() throws Exception {
        measure("GET /community/board/mylist", () ->
                mockMvc.perform(get("/community/board/mylist")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .with(authentication(userAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));
    }

    /**
     * 마이페이지: 내 게시글 검색
     * 영향받는 메서드: BoardRepositoryCustomImpl.searchAllBoardsForPagingByMe
     * 버그: .from(board) 중복 → CROSS JOIN
     */
    @Test
    @Order(9)
    @DisplayName("[CROSS JOIN] POST /community/board/mylist/search — 내 게시글 검색")
    void myBoardSearch() throws Exception {
        measure("POST /community/board/mylist/search", () ->
                mockMvc.perform(post("/community/board/mylist/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"title\",\"keyword\":\"\"}")
                                .with(authentication(userAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));
    }

    // ═══════════════════════════════════════════════════════════
    //  Reply API Tests
    // ═══════════════════════════════════════════════════════════

    /**
     * 관리자 페이지: 댓글 전체 목록 조회 (커뮤니티)
     * 영향받는 메서드: ReplyRepositoryCustomImpl.getAllRepliesForPaging
     * 수정: count 쿼리에서 불필요한 leftJoin(reply.member) 제거
     */
    @Test
    @Order(10)
    @DisplayName("[COUNT FIX] GET /reply/admin/all — 관리자 댓글 목록 (community)")
    void adminReplyListCommunity() throws Exception {
        measure("GET /reply/admin/all (community)", () ->
                mockMvc.perform(get("/reply/admin/all")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .param("postType", "community")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));
    }

    /**
     * 관리자 페이지: 댓글 전체 목록 조회 (북리뷰)
     */
    @Test
    @Order(11)
    @DisplayName("[COUNT FIX] GET /reply/admin/all — 관리자 댓글 목록 (bookreview)")
    void adminReplyListBookreview() throws Exception {
        measure("GET /reply/admin/all (bookreview)", () ->
                mockMvc.perform(get("/reply/admin/all")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .param("postType", "bookreview")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));
    }

    /**
     * 관리자 페이지: 댓글 검색 (커뮤니티)
     * 영향받는 메서드: ReplyRepositoryCustomImpl.searchAllRepliesForPaging
     * 이 메서드는 CROSS JOIN 버그 없음 — 기준선
     */
    @Test
    @Order(12)
    @DisplayName("[BASELINE] POST /reply/admin/search — 관리자 댓글 검색 (community)")
    void adminReplySearch() throws Exception {
        measure("POST /reply/admin/search (community)", () ->
                mockMvc.perform(post("/reply/admin/search")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .param("postType", "community")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"keywordType\":\"content\",\"keyword\":\"\"}")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));
    }

    // ═══════════════════════════════════════════════════════════
    //  Cache Hit Tests — 동일 요청 2회 연속 시 캐시 효과 측정
    // ═══════════════════════════════════════════════════════════

    /**
     * 캐시 히트 시나리오: 관리자 게시글 목록 (첫 요청 → 캐시 미스, 두 번째 요청 → 캐시 히트)
     * 기대: 두 번째 요청은 ~5~50ms (캐시에서 직접 반환)
     */
    @Test
    @Order(20)
    @DisplayName("[CACHE HIT] GET /community/board/admin/all — 캐시 히트 시나리오")
    void adminBoardListCacheHit() throws Exception {
        // 첫 번째 요청 (캐시 미스 — DB 조회)
        measure("GET /board/admin/all (1st, cache MISS)", () ->
                mockMvc.perform(get("/community/board/admin/all")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));

        // 두 번째 요청 (캐시 히트 — 메모리에서 반환)
        measure("GET /board/admin/all (2nd, cache HIT)", () ->
                mockMvc.perform(get("/community/board/admin/all")
                                .param("pageNum", "1")
                                .param("pageSize", "10")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));
    }

    /**
     * 캐시 히트 시나리오: 관리자 댓글 목록 (community)
     */
    @Test
    @Order(21)
    @DisplayName("[CACHE HIT] GET /reply/admin/all — 캐시 히트 시나리오 (community)")
    void adminReplyListCacheHit() throws Exception {
        measure("GET /reply/admin/all community (1st, cache MISS)", () ->
                mockMvc.perform(get("/reply/admin/all")
                                .param("pageNum", "2")
                                .param("pageSize", "10")
                                .param("postType", "community")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));

        measure("GET /reply/admin/all community (2nd, cache HIT)", () ->
                mockMvc.perform(get("/reply/admin/all")
                                .param("pageNum", "2")
                                .param("pageSize", "10")
                                .param("postType", "community")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));
    }

    /**
     * 캐시 히트 시나리오: 관리자 댓글 목록 (bookreview)
     */
    @Test
    @Order(22)
    @DisplayName("[CACHE HIT] GET /reply/admin/all — 캐시 히트 시나리오 (bookreview)")
    void adminReplyListBookreviewCacheHit() throws Exception {
        measure("GET /reply/admin/all bookreview (1st, cache MISS)", () ->
                mockMvc.perform(get("/reply/admin/all")
                                .param("pageNum", "2")
                                .param("pageSize", "10")
                                .param("postType", "bookreview")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));

        measure("GET /reply/admin/all bookreview (2nd, cache HIT)", () ->
                mockMvc.perform(get("/reply/admin/all")
                                .param("pageNum", "2")
                                .param("pageSize", "10")
                                .param("postType", "bookreview")
                                .with(authentication(adminAuth())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(200)));
    }

    // ═══════════════════════════════════════════════════════════
    //  Results Output
    // ═══════════════════════════════════════════════════════════

    @AfterAll
    void writeResults() throws Exception {
        String phase = System.getProperty("test.phase", "before");
        Path dir = Paths.get("src", "test", "resources", "performance");
        Files.createDirectories(dir);

        // CSV 결과 파일
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path csvFile = dir.resolve("results_" + phase + "_" + ts + ".csv");
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(csvFile))) {
            pw.println("endpoint,avg_ms,min_ms,max_ms");
            for (String[] row : csvRows) {
                pw.printf("%s,%s,%s,%s%n", row[0], row[1], row[2], row[3]);
            }
        }

        // 콘솔 요약
        System.out.println("\n══════════════════════════════════════════════════════════════");
        System.out.printf("  Performance Results — Phase: %s%n", phase.toUpperCase());
        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.printf("  %-60s  %8s %8s %8s%n", "Endpoint", "Avg(ms)", "Min(ms)", "Max(ms)");
        System.out.println("  " + "─".repeat(90));
        for (String[] row : csvRows) {
            System.out.printf("  %-60s  %8s %8s %8s%n", row[0], row[1], row[2], row[3]);
        }
        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.println("  Results saved → " + csvFile.toAbsolutePath());
        System.out.println("══════════════════════════════════════════════════════════════");
    }
}
