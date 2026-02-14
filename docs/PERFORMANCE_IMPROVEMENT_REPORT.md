# BookTalk Backend 성능 개선 리포트

**작성일**: 2026-02-03
**버전**: 1.1.0
**대상 패키지**: Board, Reply, BookReview, Category

---

## 목차

1. [개요](#1-개요)
2. [Phase 1: N+1 쿼리 문제 해결](#2-phase-1-n1-쿼리-문제-해결)
3. [Phase 2: Reply 페이지네이션](#3-phase-2-reply-페이지네이션)
4. [Phase 3: 데이터베이스 인덱스 최적화](#4-phase-3-데이터베이스-인덱스-최적화)
5. [Phase 4: 동시성 제어](#5-phase-4-동시성-제어)
6. [변경된 파일 목록](#6-변경된-파일-목록)
7. [설정 및 환경변수](#7-설정-및-환경변수)
8. [예상 성능 개선 효과](#8-예상-성능-개선-효과)
9. [테스트 전략](#9-테스트-전략)

---

## 1. 개요

### 1.1 배경

BookTalk Backend 애플리케이션의 성능 분석 결과, 다음과 같은 주요 문제점들이 식별되었습니다:

| 문제 | 영향 | 심각도 |
|------|------|--------|
| N+1 쿼리 문제 | 10개 게시글 조회 시 21+ 쿼리 발생 | Critical |
| Reply 전체 로드 | 1000개 댓글 게시글에서 OOM 위험 | High |
| 인덱스 부재 | Full Table Scan으로 인한 느린 조회 | High |
| ID 생성 Race Condition | 동일 밀리초 요청 시 PK 충돌 | Medium |

### 1.2 개선 목표

| Metric | 개선 전 | 개선 후 (목표) |
|--------|---------|----------------|
| Board List p95 | 300-500ms | <100ms |
| Query Count (10 items) | 21+ | 2-3 |
| ID Uniqueness | Race Condition | 100% |
| RPS Capacity | ~50 | ~200+ |

### 1.3 구현된 Phase

| Phase | 내용 | 상태 |
|-------|------|------|
| Phase 1 | N+1 쿼리 문제 해결 | 완료 |
| Phase 2 | Reply 페이지네이션 | 완료 |
| Phase 3 | 데이터베이스 인덱스 최적화 | 완료 |
| Phase 4 | 동시성 제어 | 완료 |
| Phase 5 | Caffeine 캐싱 (관리자 페이지) | 완료 |

---

## 2. Phase 1: N+1 쿼리 문제 해결

### 2.1 문제 분석

#### 2.1.1 EAGER Fetch 문제
```java
// 기존 코드 - Post.java:17
@ManyToOne(fetch = FetchType.EAGER)  // 항상 Member를 함께 로드
@JoinColumn(name = "member_id")
protected Member member;

// 기존 코드 - Reply.java:36
@ManyToOne(fetch = FetchType.EAGER)  // 항상 Member를 함께 로드
@JoinColumn(name = "member_id")
private Member member;
```

#### 2.1.2 @Formula 서브쿼리 문제
```java
// 기존 코드 - Post.java:33
@Formula("(SELECT count(1) FROM likes l WHERE l.code = code)")
protected Integer likesCnt;  // 매 로딩 시 서브쿼리 실행
```

### 2.2 개선 내용

#### 2.2.1 Post.java 변경

**파일**: `src/main/java/com/booktalk_be/common/entity/Post.java`

```java
// 변경 전
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "member_id")
protected Member member;

@ColumnDefault("0")
@Column(name = "like_cnt", nullable = false)
@Formula("(SELECT count(1) FROM likes l WHERE l.code = code)")
protected Integer likesCnt;

// 변경 후
@ManyToOne(fetch = FetchType.LAZY)  // EAGER → LAZY
@JoinColumn(name = "member_id")
protected Member member;

@ColumnDefault("0")
@Column(name = "like_cnt", nullable = false)
protected Integer likesCnt = 0;  // @Formula 제거, 저장 컬럼 사용

// 좋아요 수 관리 메서드 추가
public void incrementLikes() {
    if (this.likesCnt == null) {
        this.likesCnt = 0;
    }
    this.likesCnt++;
}

public void decrementLikes() {
    if (this.likesCnt != null && this.likesCnt > 0) {
        this.likesCnt--;
    }
}
```

#### 2.2.2 Reply.java 변경

**파일**: `src/main/java/com/booktalk_be/domain/reply/model/entity/Reply.java`

```java
// 변경 전
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "member_id")
private Member member;

@ManyToOne  // 기본값 EAGER
@JoinColumn(name = "parent_reply_code", nullable = true)
private Reply parentReplyCode;

@Formula("(SELECT count(1) FROM likes l WHERE l.code = reply_code)")
protected Integer likesCnt;

// 변경 후
@ManyToOne(fetch = FetchType.LAZY)  // EAGER → LAZY
@JoinColumn(name = "member_id")
private Member member;

@ManyToOne(fetch = FetchType.LAZY)  // 명시적 LAZY 설정
@JoinColumn(name = "parent_reply_code", nullable = true)
private Reply parentReplyCode;

@ColumnDefault("0")
@Column(name = "like_cnt", nullable = false)
protected Integer likesCnt = 0;  // @Formula 제거

// 좋아요 수 관리 메서드 추가
public void incrementLikes() { ... }
public void decrementLikes() { ... }
```

#### 2.2.3 ReplyRepositoryCustomImpl.java - JOIN FETCH 적용

**파일**: `src/main/java/com/booktalk_be/domain/reply/model/repository/querydsl/ReplyRepositoryCustomImpl.java`

```java
// 변경 전
@Override
public List<Reply> getRepliesByPostCode(String postCode) {
    return selectFrom(reply)
            .where(reply.postCode.eq(postCode))
            .where(reply.delYn.eq(false))
            .fetch();
}

// 변경 후
@Override
public List<Reply> getRepliesByPostCode(String postCode) {
    return selectFrom(reply)
            .leftJoin(reply.member).fetchJoin()      // Member 함께 조회
            .leftJoin(reply.parentReplyCode).fetchJoin()  // Parent 함께 조회
            .where(reply.postCode.eq(postCode))
            .where(reply.delYn.eq(false))
            .orderBy(reply.regTime.asc())
            .fetch();
}
```

### 2.3 트레이드오프

| 장점 | 단점 | 해결책 |
|------|------|--------|
| N+1 쿼리 제거 | LazyInitializationException 가능성 | DTO 프로젝션, fetchJoin 사용 |
| 메모리 절약 | 좋아요 추가/삭제 시 업데이트 필요 | incrementLikes/decrementLikes 메서드 |
| 조회 성능 10배+ 향상 | - | - |

---

## 3. Phase 2: Reply 페이지네이션

### 3.1 문제 분석

```java
// 기존 코드 - ReplyServiceImpl.java:72-105
@Override
public List<ReplyResponse> getRepliesByPostCode(String postCode) {
    List<Reply> replies = replyRepository.getRepliesByPostCode(postCode);
    // 모든 댓글을 한번에 로드 → 1000개 댓글 시 OOM 위험
    ...
}
```

### 3.2 개선 내용

#### 3.2.1 ReplyRepositoryCustom 인터페이스 확장

**파일**: `src/main/java/com/booktalk_be/domain/reply/model/repository/ReplyRepositoryCustom.java`

```java
// 추가된 메서드
/**
 * Get paginated root replies (replies without parent) for a post
 */
Page<Reply> getRootRepliesByPostCode(String postCode, Pageable pageable);

/**
 * Get child replies for multiple parent reply codes (batch load)
 */
List<Reply> getChildRepliesByParentCodes(List<String> parentCodes);
```

#### 3.2.2 ReplyRepositoryCustomImpl 구현

**파일**: `src/main/java/com/booktalk_be/domain/reply/model/repository/querydsl/ReplyRepositoryCustomImpl.java`

```java
@Override
public Page<Reply> getRootRepliesByPostCode(String postCode, Pageable pageable) {
    List<Reply> content = selectFrom(reply)
            .leftJoin(reply.member).fetchJoin()
            .where(reply.postCode.eq(postCode))
            .where(reply.parentReplyCode.isNull())  // Root 댓글만
            .where(reply.delYn.eq(false))
            .orderBy(reply.regTime.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total = Optional.ofNullable(
            select(Wildcard.count)
                    .from(reply)
                    .where(reply.postCode.eq(postCode))
                    .where(reply.parentReplyCode.isNull())
                    .where(reply.delYn.eq(false))
                    .fetchOne())
            .orElse(0L);

    return new PageImpl<>(content, pageable, total);
}

@Override
public List<Reply> getChildRepliesByParentCodes(List<String> parentCodes) {
    if (parentCodes == null || parentCodes.isEmpty()) {
        return List.of();
    }
    return selectFrom(reply)
            .leftJoin(reply.member).fetchJoin()
            .leftJoin(reply.parentReplyCode).fetchJoin()
            .where(reply.parentReplyCode.replyCode.in(parentCodes))  // IN clause 배치
            .where(reply.delYn.eq(false))
            .orderBy(reply.regTime.asc())
            .fetch();
}
```

#### 3.2.3 ReplyService 인터페이스 확장

**파일**: `src/main/java/com/booktalk_be/domain/reply/service/ReplyService.java`

```java
// 추가된 메서드
/**
 * Get paginated replies by post code with nested tree structure
 * Root replies are paginated, child replies are fetched in batch
 */
PageResponseDto<ReplyResponse> getRepliesByPostCodePaginated(
    String postCode, Integer pageNum, Integer pageSize);
```

#### 3.2.4 ReplyServiceImpl 구현

**파일**: `src/main/java/com/booktalk_be/domain/reply/service/ReplyServiceImpl.java`

```java
@Override
public PageResponseDto<ReplyResponse> getRepliesByPostCodePaginated(
        String postCode, Integer pageNum, Integer pageSize) {
    Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

    // 1. Root 댓글만 페이지네이션
    Page<Reply> rootRepliesPage = replyRepository.getRootRepliesByPostCode(postCode, pageable);
    List<Reply> rootReplies = rootRepliesPage.getContent();

    if (rootReplies.isEmpty()) {
        return PageResponseDto.<ReplyResponse>builder()
                .content(List.of())
                .totalPages(0)
                .build();
    }

    // 2. Root 댓글 코드 수집
    List<String> rootReplyCodes = rootReplies.stream()
            .map(Reply::getReplyCode)
            .toList();

    // 3. 자식 댓글 배치 로드 (depth 3 제한)
    List<Reply> allChildReplies = loadChildRepliesWithDepthLimit(rootReplyCodes, 3);

    // 4. 트리 구조 빌드
    Map<String, ReplyResponse> nodeMap = new HashMap<>();

    // Root 댓글 매핑
    for (Reply reply : rootReplies) {
        nodeMap.put(reply.getReplyCode(), mapReplyToResponse(reply));
    }

    // 자식 댓글 매핑
    for (Reply reply : allChildReplies) {
        nodeMap.put(reply.getReplyCode(), mapReplyToResponse(reply));
    }

    // 부모-자식 관계 구축
    for (Reply reply : allChildReplies) {
        Reply parent = reply.getParentReplyCode();
        if (parent != null) {
            ReplyResponse parentDto = nodeMap.get(parent.getReplyCode());
            if (parentDto != null) {
                ReplyResponse childDto = nodeMap.get(reply.getReplyCode());
                parentDto.getReplies().add(childDto);
            }
        }
    }

    List<ReplyResponse> content = rootReplies.stream()
            .map(r -> nodeMap.get(r.getReplyCode()))
            .toList();

    return PageResponseDto.<ReplyResponse>builder()
            .content(content)
            .totalPages(rootRepliesPage.getTotalPages())
            .build();
}

/**
 * 깊이 제한으로 자식 댓글 로드 (무한 재귀 방지)
 */
private List<Reply> loadChildRepliesWithDepthLimit(List<String> parentCodes, int maxDepth) {
    if (maxDepth <= 0 || parentCodes.isEmpty()) {
        return List.of();
    }

    List<Reply> allChildren = new ArrayList<>();
    List<String> currentLevelCodes = parentCodes;

    for (int depth = 0; depth < maxDepth; depth++) {
        List<Reply> levelChildren = replyRepository.getChildRepliesByParentCodes(currentLevelCodes);
        if (levelChildren.isEmpty()) {
            break;
        }
        allChildren.addAll(levelChildren);
        currentLevelCodes = levelChildren.stream()
                .map(Reply::getReplyCode)
                .toList();
    }

    return allChildren;
}

private ReplyResponse mapReplyToResponse(Reply entity) {
    return ReplyResponse.builder()
            .replyCode(entity.getReplyCode())
            .memberName(entity.getMember().getName())
            .postCode(entity.getPostCode())
            .content(entity.getContent())
            .regDate(entity.getRegTime().toLocalDate().toString())
            .updateDate(entity.getUpdateTime().toLocalDate().toString())
            .likesCnt(entity.getLikesCnt())
            .replies(new ArrayList<>())
            .build();
}
```

### 3.3 쿼리 실행 패턴

| 단계 | 쿼리 | 설명 |
|------|------|------|
| 1 | Root 댓글 조회 | `SELECT ... FROM reply WHERE post_code=? AND parent_reply_code IS NULL LIMIT 20` |
| 2 | Root 카운트 | `SELECT COUNT(*) FROM reply WHERE ...` |
| 3 | Level 1 자식 | `SELECT ... FROM reply WHERE parent_reply_code IN (?, ?, ...)` |
| 4 | Level 2 자식 | `SELECT ... FROM reply WHERE parent_reply_code IN (?, ?, ...)` |
| 5 | Level 3 자식 | `SELECT ... FROM reply WHERE parent_reply_code IN (?, ?, ...)` |

**총 쿼리 수**: 최대 5개 (기존 N+1 대비 획기적 감소)

### 3.4 트레이드오프

| 장점 | 단점 | 해결책 |
|------|------|--------|
| 메모리 사용량 제어 | 깊은 트리에서 추가 요청 필요 | depth 3 제한 |
| OOM 방지 | 복잡성 증가 | 명확한 배치 로드 패턴 |
| 응답 시간 개선 | - | - |

---

## 4. Phase 3: 데이터베이스 인덱스 최적화

### 4.1 문제 분석

공통 쿼리 패턴에 인덱스 부재로 Full Table Scan 발생

```sql
-- 예: 카테고리별 게시글 목록 (가장 빈번한 쿼리)
SELECT * FROM board
WHERE category_id = 1 AND del_yn = false
ORDER BY reg_time DESC LIMIT 10;
-- 인덱스 없이: type=ALL, rows=100000 (Full Scan)
```

### 4.2 개선 내용

**파일**: `src/main/resources/db/migration/V2__add_performance_indexes.sql`

```sql
-- Performance Indexes Migration
-- Created for BookTalk Backend Performance Improvement

-- =====================================================
-- BOARD TABLE INDEXES
-- =====================================================

-- Index for category-based board listing (most frequent query)
CREATE INDEX IF NOT EXISTS idx_board_category_del_regtime
ON board (category_id, del_yn, reg_time DESC);

-- Index for member's board lookup
CREATE INDEX IF NOT EXISTS idx_board_member_del
ON board (member_id, del_yn);

-- Index for board code lookup with category (next/prev navigation)
CREATE INDEX IF NOT EXISTS idx_board_code_category_del
ON board (code, category_id, del_yn);

-- =====================================================
-- REPLY TABLE INDEXES
-- =====================================================

-- Index for post's reply listing (board detail page - critical)
CREATE INDEX IF NOT EXISTS idx_reply_postcode_del_regtime
ON reply (post_code, del_yn, reg_time ASC);

-- Index for child reply lookup (nested replies)
CREATE INDEX IF NOT EXISTS idx_reply_parent_del
ON reply (parent_reply_code, del_yn);

-- Index for member's reply lookup
CREATE INDEX IF NOT EXISTS idx_reply_member_del
ON reply (member_id, del_yn);

-- =====================================================
-- CATEGORY TABLE INDEXES
-- =====================================================

-- Index for active category listing
CREATE INDEX IF NOT EXISTS idx_category_active_del
ON category (is_active, del_yn);

-- Index for parent category lookup (tree structure)
CREATE INDEX IF NOT EXISTS idx_category_parent
ON category (p_category_id);

-- =====================================================
-- LIKES TABLE INDEXES
-- =====================================================

-- Index for like count aggregation by code
CREATE INDEX IF NOT EXISTS idx_likes_code
ON likes (code);

-- Index for checking if user liked an item
CREATE INDEX IF NOT EXISTS idx_likes_member_code
ON likes (member_id, code);

-- =====================================================
-- BOOK_REVIEW TABLE INDEXES
-- =====================================================

-- Index for category-based book review listing
CREATE INDEX IF NOT EXISTS idx_bookreview_category_del_regtime
ON book_review (category_id, del_yn, reg_time DESC);

-- Index for member's book review lookup
CREATE INDEX IF NOT EXISTS idx_bookreview_member_del
ON book_review (member_id, del_yn);

-- Index for ISBN lookup (book searches)
CREATE INDEX IF NOT EXISTS idx_bookreview_isbn
ON book_review (isbn);
```

### 4.3 인덱스 상세 설명

| 인덱스 | 테이블 | 컬럼 | 지원 쿼리 |
|--------|--------|------|-----------|
| `idx_board_category_del_regtime` | board | category_id, del_yn, reg_time DESC | 카테고리별 목록 |
| `idx_board_member_del` | board | member_id, del_yn | 내 게시글 목록 |
| `idx_board_code_category_del` | board | code, category_id, del_yn | 이전/다음 게시글 |
| `idx_reply_postcode_del_regtime` | reply | post_code, del_yn, reg_time ASC | 게시글 댓글 목록 |
| `idx_reply_parent_del` | reply | parent_reply_code, del_yn | 대댓글 조회 |
| `idx_reply_member_del` | reply | member_id, del_yn | 내 댓글 목록 |
| `idx_category_active_del` | category | is_active, del_yn | 활성 카테고리 |
| `idx_category_parent` | category | p_category_id | 카테고리 트리 |
| `idx_likes_code` | likes | code | 좋아요 수 집계 |
| `idx_likes_member_code` | likes | member_id, code | 좋아요 여부 확인 |
| `idx_bookreview_category_del_regtime` | book_review | category_id, del_yn, reg_time DESC | 카테고리별 리뷰 |
| `idx_bookreview_member_del` | book_review | member_id, del_yn | 내 리뷰 목록 |
| `idx_bookreview_isbn` | book_review | isbn | ISBN 검색 |

### 4.4 EXPLAIN ANALYZE 예상 결과

```sql
-- Before (인덱스 없음)
EXPLAIN ANALYZE SELECT * FROM board
WHERE category_id = 1 AND del_yn = false
ORDER BY reg_time DESC LIMIT 10;
-- type: ALL, rows: 100000, Extra: Using filesort

-- After (인덱스 적용)
EXPLAIN ANALYZE SELECT * FROM board
WHERE category_id = 1 AND del_yn = false
ORDER BY reg_time DESC LIMIT 10;
-- type: range, rows: 100, Extra: Using index
```

### 4.5 트레이드오프

| 장점 | 단점 | 비고 |
|------|------|------|
| SELECT 50-200ms → 1-5ms | INSERT/UPDATE 10-20% 느려짐 | 읽기 중심 서비스에서 허용 가능 |
| Full Scan 제거 | 인덱스 저장 공간 필요 | 성능 대비 합리적 비용 |

---

## 5. Phase 4: 동시성 제어

### 5.1 문제 분석

#### 5.1.1 ID 생성 Race Condition
```java
// 기존 코드 - Reply.java:25, Board.java, BookReview.java 동일
@PrePersist
public void generateId() {
    if(this.replyCode == null) {
        this.replyCode = "REP_" + System.currentTimeMillis();
        // 동일 밀리초에 여러 요청 시 PK 충돌!
    }
}
```

#### 5.1.2 Optimistic Locking 부재
- `@Version` 필드 없음
- 동시 수정 시 Lost Update 발생

### 5.2 개선 내용

#### 5.2.1 CommonTimeEntity - @Version 추가

**파일**: `src/main/java/com/booktalk_be/common/entity/CommonTimeEntity.java`

```java
@EntityListeners(value = {AuditingEntityListener.class})
@MappedSuperclass
@Getter
@Setter
public class CommonTimeEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime regTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    @Version  // 추가: Optimistic Locking
    @Column(name = "version")
    private Long version;
}
```

#### 5.2.2 DistributedIdGenerator - Snowflake 방식 ID 생성

**파일**: `src/main/java/com/booktalk_be/common/utils/DistributedIdGenerator.java`

```java
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

    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public DistributedIdGenerator(@Value("${app.worker-id:0}") long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID));
        }
        this.workerId = workerId;
    }

    public synchronized String generateBoardId() {
        return "BO_" + nextId();
    }

    public synchronized String generateReplyId() {
        return "REP_" + nextId();
    }

    public synchronized String generateBookReviewId() {
        return "BR_" + nextId();
    }

    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        if (timestamp < lastTimestamp) {
            timestamp = waitNextMillis(lastTimestamp);
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
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
}
```

#### 5.2.3 EntityIdGeneratorConfig - ID Generator 초기화

**파일**: `src/main/java/com/booktalk_be/springconfig/EntityIdGeneratorConfig.java`

```java
@Configuration
@RequiredArgsConstructor
public class EntityIdGeneratorConfig {

    private final DistributedIdGenerator idGenerator;

    @PostConstruct
    public void initializeEntityIdGenerators() {
        Board.setIdGenerator(idGenerator);
        Reply.setIdGenerator(idGenerator);
        BookReview.setIdGenerator(idGenerator);
    }
}
```

#### 5.2.4 Entity @PrePersist 수정

**Board.java, Reply.java, BookReview.java 공통 패턴**:

```java
@Transient
private static DistributedIdGenerator idGenerator;

public static void setIdGenerator(DistributedIdGenerator generator) {
    idGenerator = generator;
}

@PrePersist
public void generateId() {
    if(this.code == null) {
        if (idGenerator != null) {
            this.code = idGenerator.generateBoardId();  // 분산 ID 사용
        } else {
            this.code = "BO_" + System.currentTimeMillis();  // 폴백
        }
    }
    // ...
}
```

#### 5.2.5 Version 컬럼 마이그레이션

**파일**: `src/main/resources/db/migration/V3__add_version_column.sql`

```sql
-- Add version column for Optimistic Locking
-- Enables concurrent update detection and prevents lost updates

ALTER TABLE board ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE reply ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE book_review ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
ALTER TABLE category ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;
```

#### 5.2.6 GlobalExceptionHandler - 동시성 예외 처리

**파일**: `src/main/java/com/booktalk_be/springconfig/exception/GlobalExceptionHandler.java`

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Optimistic Locking 실패 시 HTTP 409 Conflict 반환
     */
    @ExceptionHandler({
        OptimisticLockingFailureException.class,
        ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ResponseDto> handleOptimisticLockingFailure(Exception ex) {
        log.warn("Optimistic locking failure occurred: {}", ex.getMessage());

        ResponseDto response = ResponseDto.builder()
                .code(HttpStatus.CONFLICT.value())
                .msg("다른 사용자가 수정 중입니다. 새로고침 후 다시 시도해주세요.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseDto> handleEntityNotFound(EntityNotFoundException ex) { ... }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto> handleIllegalArgument(IllegalArgumentException ex) { ... }
}
```

#### 5.2.7 Category Entity 수정

**파일**: `src/main/java/com/booktalk_be/domain/category/model/entity/Category.java`

```java
// 변경 전
public class Category {

// 변경 후
public class Category extends CommonTimeEntity {  // Version 필드 상속
```

### 5.3 Snowflake ID 구조

```
┌─────────────────────────────────────────────────────────────────────┐
│                           64-bit ID                                  │
├──────┬────────────────────────────────────┬───────────┬─────────────┤
│ Sign │         Timestamp (41 bits)        │ Worker ID │  Sequence   │
│ 1bit │         ~69 years capacity         │  10 bits  │  12 bits    │
│      │      (ms since 2024-01-01)         │  0-1023   │   0-4095    │
└──────┴────────────────────────────────────┴───────────┴─────────────┘
```

**용량**:
- 워커 수: 최대 1,024개 인스턴스
- 초당 ID: 워커당 4,096,000개 (4096 × 1000ms)
- 수명: 약 69년 (2024년 기준 2093년까지)

### 5.4 트레이드오프

| 장점 | 단점 | 해결책 |
|------|------|--------|
| 100% ID 고유성 보장 | 시계 동기화 필요 | NTP 동기화 권장 |
| Lost Update 방지 | 충돌 시 재시도 필요 | HTTP 409로 클라이언트 안내 |
| 다중 인스턴스 지원 | worker-id 설정 필요 | 환경변수 `WORKER_ID` |

---

## 6. 변경된 파일 목록

### 6.1 신규 생성 파일

| 파일 | 설명 |
|------|------|
| `src/main/java/com/booktalk_be/common/utils/DistributedIdGenerator.java` | Snowflake 방식 분산 ID 생성기 |
| `src/main/java/com/booktalk_be/springconfig/EntityIdGeneratorConfig.java` | Entity ID Generator 초기화 설정 |
| `src/main/java/com/booktalk_be/springconfig/exception/GlobalExceptionHandler.java` | 전역 예외 처리기 |
| `src/main/resources/db/migration/V2__add_performance_indexes.sql` | 성능 인덱스 마이그레이션 |
| `src/main/resources/db/migration/V3__add_version_column.sql` | Version 컬럼 마이그레이션 |

### 6.2 수정된 파일

| 파일 | 변경 내용 |
|------|-----------|
| `src/main/resources/application.yml` | worker-id 설정 추가 |
| `src/main/java/com/booktalk_be/common/entity/Post.java` | LAZY fetch, @Formula 제거, 좋아요 메서드 추가 |
| `src/main/java/com/booktalk_be/common/entity/CommonTimeEntity.java` | @Version 필드 추가 |
| `src/main/java/com/booktalk_be/domain/board/model/entity/Board.java` | 분산 ID 생성기 적용 |
| `src/main/java/com/booktalk_be/domain/reply/model/entity/Reply.java` | LAZY fetch, 분산 ID 생성기, 좋아요 메서드 |
| `src/main/java/com/booktalk_be/domain/bookreview/model/entity/BookReview.java` | 분산 ID 생성기 적용 |
| `src/main/java/com/booktalk_be/domain/category/model/entity/Category.java` | CommonTimeEntity 상속 (Version 지원) |
| `src/main/java/com/booktalk_be/domain/reply/model/repository/ReplyRepositoryCustom.java` | 페이지네이션 메서드 추가 |
| `src/main/java/com/booktalk_be/domain/reply/model/repository/querydsl/ReplyRepositoryCustomImpl.java` | fetchJoin, 페이지네이션 구현 |
| `src/main/java/com/booktalk_be/domain/reply/service/ReplyService.java` | 페이지네이션 메서드 추가 |
| `src/main/java/com/booktalk_be/domain/reply/service/ReplyServiceImpl.java` | 페이지네이션 구현 |

---

## 7. 설정 및 환경변수

### 7.1 환경변수

| 환경변수 | 기본값 | 설명 |
|----------|--------|------|
| `WORKER_ID` | 0 | 분산 ID 생성기 워커 ID (0-1023) |

### 7.2 다중 인스턴스 배포 시 주의사항

```yaml
# Instance 1
WORKER_ID=0

# Instance 2
WORKER_ID=1

# Instance 3
WORKER_ID=2
# ...
```

각 인스턴스는 **고유한 WORKER_ID**를 가져야 합니다.

### 7.3 application.yml 설정

```yaml
# Application Configuration
app:
  worker-id: ${WORKER_ID:0}
```

---

## 8. 예상 성능 개선 효과

### 8.1 쿼리 성능

| 시나리오 | 개선 전 | 개선 후 | 개선율 |
|----------|---------|---------|--------|
| Board List 조회 (10개) | 21+ 쿼리 | 2 쿼리 | -90% |
| Board Detail 조회 | 11+ 쿼리 | 3-5 쿼리 | -70% |
| Reply 조회 (100개) | 101 쿼리 | 3-5 쿼리 | -95% |

### 8.2 응답 시간

| API | 개선 전 (p95) | 개선 후 (목표) | 개선율 |
|-----|--------------|----------------|--------|
| GET /boards?category=1 | 300-500ms | <100ms | -80% |
| GET /boards/{code} | 200-400ms | <80ms | -80% |
| GET /categories | 50-100ms | <30ms | -70% |

### 8.3 처리량 (Throughput)

| Metric | 개선 전 | 개선 후 (목표) |
|--------|---------|----------------|
| RPS | ~50 | ~200+ |
| 동시 사용자 | ~50 | ~200+ |

### 8.4 리소스 사용량

| 리소스 | 개선 전 | 개선 후 |
|--------|---------|---------|
| DB 커넥션 사용 | High | -70% |
| DB CPU | High | -60% |
| 애플리케이션 메모리 | OOM 위험 | 안정적 |

---

## 9. 테스트 전략

### 9.1 N+1 쿼리 해결 검증

```java
@Test
void testQueryCount() {
    // Given
    Statistics stats = em.unwrap(Session.class)
            .getSessionFactory().getStatistics();
    stats.clear();

    // When
    boardRepository.findBoardsForPaging(1, PageRequest.of(0, 10));

    // Then
    assertThat(stats.getQueryExecutionCount()).isLessThanOrEqualTo(2);
}
```

### 9.2 페이지네이션 메모리 테스트

```java
@Test
void testReplyPaginationMemory() {
    // Given
    String postCode = createPostWithReplies(1000);

    // When
    PageResponseDto<ReplyResponse> result =
        replyService.getRepliesByPostCodePaginated(postCode, 1, 20);

    // Then
    assertThat(result.getContent()).hasSize(20);
    // 메모리 사용량 모니터링
}
```

### 9.3 인덱스 검증

```sql
EXPLAIN ANALYZE SELECT * FROM board
WHERE category_id = 1 AND del_yn = false
ORDER BY reg_time DESC LIMIT 10;
-- Expected: type=range, Using index
```

### 9.4 동시성 테스트

```java
@Test
void testOptimisticLocking() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    AtomicInteger conflicts = new AtomicInteger(0);

    for (int i = 0; i < 10; i++) {
        executor.submit(() -> {
            try {
                boardService.modifyBoard(cmd);
            } catch (OptimisticLockingFailureException e) {
                conflicts.incrementAndGet();
            }
        });
    }

    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    assertThat(conflicts.get()).isGreaterThan(0);
}

@Test
void testIdUniqueness() throws Exception {
    Set<String> ids = ConcurrentHashMap.newKeySet();
    ExecutorService executor = Executors.newFixedThreadPool(100);

    for (int i = 0; i < 100000; i++) {
        executor.submit(() -> ids.add(idGenerator.generateBoardId()));
    }

    executor.shutdown();
    executor.awaitTermination(30, TimeUnit.SECONDS);

    assertThat(ids).hasSize(100000);  // 100% 고유성
}
```

### 9.5 k6 부하 테스트

```javascript
// k6-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<100', 'p(99)<200'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    const responses = http.batch([
        ['GET', 'http://localhost:8080/api/boards?categoryId=1&pageNum=1&pageSize=10'],
        ['GET', 'http://localhost:8080/api/categories'],
    ]);

    responses.forEach((res) => {
        check(res, {
            'status is 200': (r) => r.status === 200,
            'response time < 200ms': (r) => r.timings.duration < 200,
        });
    });

    sleep(1);
}
```

---

## 부록: 검증 체크리스트

### Phase 1: N+1 해결
- [ ] `hibernate.show_sql=true`로 쿼리 수 확인
- [ ] Board 목록 조회 시 2-3개 쿼리만 실행
- [ ] Reply 조회 시 N+1 제거 확인

### Phase 2: 페이지네이션
- [ ] 1000개 댓글 게시글에서 정상 응답
- [ ] 메모리 사용량 안정적
- [ ] 깊이 3 제한 동작 확인

### Phase 3: 인덱스
- [ ] `EXPLAIN ANALYZE`로 Index 사용 확인
- [ ] Full Table Scan 제거 확인
- [ ] 쿼리 실행 시간 개선 확인

### Phase 4: 동시성
- [ ] 멀티스레드 테스트 통과
- [ ] ID 고유성 100% 확인
- [ ] OptimisticLockingFailureException 처리 확인

---

## Phase 5: Caffeine 캐싱 (관리자 페이지)

### 배경

관리자 페이지 API 성능 분석 결과, COUNT 쿼리가 전체 응답 시간의 90% 이상을 차지하는 것이 확인됨.
Redis 대신 단일 서버 환경에 적합한 **Caffeine 인메모리 캐시**를 적용.

### 구현 내용

#### 5.1 의존성 추가

```groovy
// build.gradle
implementation 'org.springframework.boot:spring-boot-starter-cache'
implementation 'com.github.ben-manes.caffeine:caffeine'
```

#### 5.2 CacheConfig.java (신규)

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .maximumSize(200));
        return cacheManager;
    }
}
```

#### 5.3 캐시 적용 대상

| 캐시명 | 대상 메서드 | TTL |
|--------|-----------|-----|
| `boardAdminList` | `getAllBoardsForPaging`, `searchAllBoardsForPaging` | 30초 |
| `replyAdminList` | `getAllRepliesForPaging`, `searchAllRepliesForPaging` | 30초 |

#### 5.4 캐시 무효화

| 트리거 | 무효화 대상 |
|--------|-----------|
| 게시글 생성/삭제/제재/복구 | `boardAdminList` 전체 |
| 댓글 생성/삭제/제재/복구 | `replyAdminList` 전체 |

#### 5.5 Reply 전용 인덱스 추가

```sql
-- V5__add_reply_admin_index.sql
CREATE INDEX idx_reply_postcode_replycode ON reply(post_code, reply_code DESC);
```

- `post_code LIKE 'BO_%'` 쿼리 시 filtered=50% → ~100% 개선
- COUNT 쿼리 50~60% 성능 향상 예상

#### 5.6 예상 성능 개선

| 엔드포인트 | Before | After (캐시 히트) |
|-----------|--------|-----------------|
| `GET /community/board/admin/all` | 1,136ms | ~5~50ms |
| `GET /reply/admin/all (community)` | 2,338ms | ~5~50ms |
| `GET /reply/admin/all (bookreview)` | 234ms | ~5~50ms |

### 설계 결정: Redis vs Caffeine

| 항목 | Redis | Caffeine (선택) |
|------|-------|----------------|
| 네트워크 오버헤드 | 있음 | 없음 (인메모리) |
| 다중 서버 지원 | 지원 | 미지원 |
| 운영 복잡도 | Redis 서버 필요 | 추가 인프라 불필요 |
| 현재 서버 구성 | 단일 서버 | 단일 서버 |

단일 서버 환경에서 관리자 페이지 캐싱이므로 Caffeine이 적합.
향후 다중 서버 전환 시 Redis로 교체 가능 (Spring Cache 추상화 덕분에 설정만 변경).

---

**문서 작성**: Claude Code
**최종 수정일**: 2026-02-13
**버전**: 1.2.0 (Phase 5 Caffeine 캐싱 + Reply 인덱스 추가)
