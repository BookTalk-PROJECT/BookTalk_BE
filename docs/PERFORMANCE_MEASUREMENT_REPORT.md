# BookTalk Backend 성능 개선 측정 리포트

## 테스트 환경

| 항목 | 값 |
|------|-----|
| Java | OpenJDK 17.0.16 |
| Spring Boot | 3.4.4 |
| Hibernate ORM | 6.6.11.Final |
| MySQL | 8.0 |
| OS | Windows 10/11 |
| Test Framework | JUnit 5 + Spring Boot Test |
| 측정일 | 2026-02-03 |

### 데이터 규모 (DummyDataGeneratorTest 기준)

| 테이블 | 레코드 수 |
|--------|-----------|
| Member | ~1,000 |
| Category | ~100 |
| Board | ~100,000 (195,250 rows scanned) |
| BookReview | ~100,000 (194,768 rows scanned) |
| Reply | ~700,000 (1,290,411 rows scanned) |
| Likes | ~100,000 |

### 주요 Hibernate 설정

| 설정 | 값 | 영향 |
|------|-----|------|
| `hibernate.default_batch_fetch_size` | 100 | Lazy 로딩 시 IN 쿼리로 배치 처리 (N+1 완화) |
| `open-in-view` | false | 트랜잭션 바깥에서 Lazy 로딩 불가 |

---

## Phase 1: N+1 쿼리 해결 및 @Formula 제거

### 1-1. N+1 쿼리 해결 (fetchJoin 적용)

**개선 내용**: Reply 조회 시 Member 엔티티에 대한 N+1 문제를 `LEFT JOIN FETCH`로 해결

**측정 방법**: fetchJoin 없는 JPQL vs fetchJoin 있는 Repository 메서드 비교

| 항목 | 개선 전 (fetchJoin 없음) | 개선 후 (fetchJoin 적용) |
|------|-------------------------|------------------------|
| 초기 쿼리 수 | 1 | 1 |
| Member 접근 후 총 쿼리 수 | 1 + N (최대 50개 추가) | 1 (추가 쿼리 없음) |
| 이론적 쿼리 수 (batch_fetch_size 없을 때) | 1 + 고유 Member 수 | 1 |
| 실측 쿼리 수 (batch_fetch_size=100 적용) | 1 + ceil(N/100) | 1 |
| 로드된 Reply 수 | 50 (LIMIT) | 전체 |

**핵심 개선 사항**:
- `@ManyToOne(fetch = FetchType.LAZY)` + `LEFT JOIN FETCH`로 Member를 한 번에 로드
- Member 필드 접근 시 추가 쿼리 **0건** (fetchJoin이 정상 동작)
- Reply, Member, ParentReply를 단일 JOIN 쿼리로 해결

**관련 코드**:
- `ReplyRepositoryCustomImpl.getRepliesByPostCode()`: `leftJoin(reply.member).fetchJoin().leftJoin(reply.parentReplyCode).fetchJoin()`

#### 한계점 및 주의사항

> **`hibernate.default_batch_fetch_size=100` 이 N+1을 자동 완화합니다.**
>
> `application.yml`에 설정된 `default_batch_fetch_size: 100` 덕분에, fetchJoin 없이도 Hibernate가
> N개의 Lazy 로딩을 `ceil(N/100)`개의 `IN` 쿼리로 묶어 처리합니다. 따라서 실측에서 fetchJoin
> 유무에 따른 쿼리 수 차이가 미미할 수 있습니다.
>
> **그럼에도 fetchJoin이 더 나은 이유:**
> - `batch_fetch_size=100`: 메인 1쿼리 + `ceil(N/100)` IN 쿼리 = **최소 2 round-trip**
> - `fetchJoin`: JOIN 1쿼리 = **항상 1 round-trip**
> - 데이터 규모에 관계없이 fetchJoin은 DB round-trip을 최소화합니다.
> - batch_fetch_size는 "N+1 → 1+few"로 완화할 뿐, fetchJoin처럼 "1"로 줄이지는 못합니다.

---

### 1-2. @Formula 서브쿼리 제거 (저장 컬럼 전환)

**개선 내용**: Board의 `likesCnt`를 `@Formula` 서브쿼리에서 저장 컬럼으로 변경

**측정 방법**: 서브쿼리 포함 네이티브 쿼리 vs 저장 컬럼 네이티브 쿼리 (10회 평균)

| 항목 | 개선 전 (@Formula 서브쿼리) | 개선 후 (저장 컬럼) |
|------|---------------------------|-------------------|
| 평균 응답 시간 | 3.1ms | 0.5ms |
| 개별 실행 시간 | [26, 3, 1, 0, 0, 0, 0, 0, 0, 1]ms | [1, 1, 1, 0, 0, 1, 1, 0, 0, 0]ms |
| **개선율** | - | **83.9%** |

**핵심 개선 사항**:
- 매 조회마다 `(SELECT COUNT(1) FROM likes WHERE ...)` 서브쿼리 실행 제거
- `like_cnt` 컬럼에 직접 저장하여 단순 컬럼 읽기로 변경
- `incrementLikes()` / `decrementLikes()` 메서드로 정합성 유지

#### 한계점 및 주의사항: like_cnt + @Version 충돌 트레이드오프

> **`like_cnt`를 Board 엔티티에 저장하면, 좋아요 시 `@Version`이 증가하여 글 수정/삭제와 충돌합니다.**
>
> **충돌 시나리오:**
> ```
> User A: Board 조회 (version=1)
> User B: Board 조회 (version=1)
> User A: 좋아요 → incrementLikes() → UPDATE board SET like_cnt=11, version=2
> User B: 제목 수정 → UPDATE board SET title='...' WHERE version=1
>   → 0 rows affected → OptimisticLockingFailureException (HTTP 409)
> ```
>
> 좋아요는 **고빈도 연산**이므로, 인기 게시글에서 글 수정이 반복적으로 실패할 수 있습니다.
>
> **Phase 1-3 테스트에서 이 충돌을 실제로 시연하고 검증했습니다.**
>
> **대안 아키텍처:**
> | 대안 | 방식 | 장점 | 단점 |
> |------|------|------|------|
> | A. 벌크 UPDATE | `UPDATE board SET like_cnt = like_cnt + 1 WHERE code = ?` | @Version 우회, 엔티티 로드 불필요 | JPA 영속성 컨텍스트와 불일치 가능 |
> | B. 카운터 테이블 분리 | `likes_count(code, count)` 별도 테이블 | 완전한 격리 | 스키마 변경 필요 |
> | C. Redis 캐시 | 외부 캐시에 카운트 보관 | 초고속, 격리 | 인프라 의존성 추가 |

---

## Phase 2: Reply 페이지네이션

**개선 내용**: 전체 Reply 로드 방식에서 루트 Reply 페이지네이션 + 자식 배치 로딩으로 전환

**측정 방법**: `getRepliesByPostCode()` vs `getRepliesByPostCodePaginated()` 직접 비교 (5회 평균)

### 2-1. 기존 테스트 데이터 결과 (소량 데이터)

| 항목 | 개선 전 (전체 로드) | 개선 후 (페이지네이션) |
|------|-------------------|---------------------|
| 평균 응답 시간 | 11.6ms | 25.8ms |
| 평균 쿼리 수 | 1.0 | 5.0 |
| 반환 레코드 수 | 12 (모든 루트 Reply) | 12 (1페이지, 총 1페이지) |

#### 한계점 및 주의사항

> **테스트 대상 게시글의 루트 Reply가 12개로, 페이지(20개) 안에 전부 들어갑니다.**
>
> 이 조건에서 페이지네이션은 전체 로드 대비 **오히려 느립니다** (추가 쿼리 오버헤드):
> - 전체 로드: fetchJoin 1쿼리
> - 페이지네이션: 루트 쿼리(1) + 카운트 쿼리(1) + 깊이별 자식 로딩(최대 3) = 최대 5쿼리
>
> 소량 데이터에서 5 round-trip > 1 round-trip은 당연한 결과이며, **이것은 결함이 아닌 예상된 동작**입니다.

### 2-2. 대량 데이터 시뮬레이션 결과 (200개 루트 Reply)

테스트에서 200개 루트 Reply를 동적 생성하여 페이지네이션의 실질적 효과를 측정합니다.

| 항목 | 개선 전 (전체 로드) | 개선 후 (페이지네이션) |
|------|-------------------|---------------------|
| 반환 레코드 수 | 200 (전체) | 20 (1페이지) |
| **데이터 전송량 감소** | - | **90%** |

### 규모별 기대 효과

| 루트 Reply 수 | 전체 로드 반환 | 페이지네이션 반환 | 전송량 감소 |
|-------------|-------------|----------------|-----------|
| 12 | 12 | 12 | 0% (이점 없음) |
| 200 | 200 | 20 | **90%** |
| 1,000 | 1,000 | 20 | **98%** |
| 10,000 | 10,000 | 20 | **99.8%** |

**추가 개선 사항**:
- 깊이 제한: 최대 3단계까지만 자식 Reply 로딩 (무한 재귀 방지)
- 배치 로딩: `getChildRepliesByParentCodes()`로 각 깊이 레벨을 단일 쿼리로 처리
- 페이지 간 중복 없음 (검증 완료)

---

## Phase 3: 인덱스 최적화

**개선 내용**: V2 마이그레이션으로 13개 성능 인덱스 추가

**측정 방법**: 실제 인덱스 DROP 후 성능 측정 -> 인덱스 재생성 후 성능 측정 (각 10회 평균, 워밍업 2회 포함)

### Board 쿼리 (`category_id = ? AND del_yn = false ORDER BY reg_time DESC LIMIT 20`)

| 항목 | 인덱스 없음 | 인덱스 있음 |
|------|-----------|-----------|
| 평균 응답 시간 | 206ms | 0ms (< 1ms) |
| EXPLAIN type | ALL (Full Table Scan) | ref (Index Lookup) |
| EXPLAIN key | null | idx_board_category_del_regtime |
| EXPLAIN rows | 195,250 | 1,501 |
| EXPLAIN Extra | Using where; Using filesort | null (인덱스 정렬) |
| **개선율** | - | **~100%** |

### Reply 쿼리 (`post_code = ? AND del_yn = false ORDER BY reg_time ASC LIMIT 50`)

| 항목 | 인덱스 없음 | 인덱스 있음 |
|------|-----------|-----------|
| 평균 응답 시간 | 808ms | 1ms |
| EXPLAIN type | ALL (Full Table Scan) | ref (Index Lookup) |
| EXPLAIN key | null | idx_reply_postcode_del_regtime |
| EXPLAIN rows | 1,290,411 | 156 |
| EXPLAIN Extra | Using where; Using filesort | null (인덱스 정렬) |
| **개선율** | - | **99.9%** |

### BookReview 쿼리 (`category_id = ? AND del_yn = false ORDER BY reg_time DESC LIMIT 20`)

| 항목 | 인덱스 없음 | 인덱스 있음 |
|------|-----------|-----------|
| 평균 응답 시간 | 183ms | 1ms |
| EXPLAIN type | ALL (Full Table Scan) | ref (Index Lookup) |
| EXPLAIN key | null | idx_bookreview_category_del_regtime |
| EXPLAIN rows | 194,768 | 1,511 |
| EXPLAIN Extra | Using where; Using filesort | null (인덱스 정렬) |
| **개선율** | - | **99.5%** |

### 인덱스 목록 (13개)

| # | 인덱스명 | 테이블 | 컬럼 |
|---|---------|--------|------|
| 1 | idx_board_category_del_regtime | board | category_id, del_yn, reg_time DESC |
| 2 | idx_board_member_del | board | member_id, del_yn |
| 3 | idx_board_code_category_del | board | code, category_id, del_yn |
| 4 | idx_reply_postcode_del_regtime | reply | post_code, del_yn, reg_time ASC |
| 5 | idx_reply_parent_del | reply | parent_reply_code, del_yn |
| 6 | idx_reply_member_del | reply | member_id, del_yn |
| 7 | idx_category_active_del | category | is_active, del_yn |
| 8 | idx_category_parent | category | p_category_id |
| 9 | idx_likes_code | likes | code |
| 10 | idx_likes_member_code | likes | member_id, code |
| 11 | idx_bookreview_category_del_regtime | book_review | category_id, del_yn, reg_time DESC |
| 12 | idx_bookreview_member_del | book_review | member_id, del_yn |
| 13 | idx_bookreview_isbn | book_review | isbn |

**인덱스 복원 검증**: DROP/CREATE 후 13개 인덱스 모두 존재 확인 완료

#### 한계점 및 주의사항

> **FK 제약 인덱스 DROP 처리**
>
> 기존 테스트에서는 4개 인덱스가 FK 제약으로 DROP 실패했습니다:
> - `idx_board_member_del` (board.member_id FK)
> - `idx_reply_parent_del` (reply.parent_reply_code FK)
> - `idx_reply_member_del` (reply.member_id FK)
> - `idx_bookreview_member_del` (book_review.member_id FK)
>
> 개선된 테스트에서는 `SET FOREIGN_KEY_CHECKS = 0`으로 FK 제약을 일시 비활성화하여
> 모든 13개 인덱스의 DROP/CREATE를 시도합니다. 핵심 쿼리(Board category, Reply postcode,
> BookReview category)에 사용되는 인덱스는 FK 무관하므로 측정값은 유효합니다.
>
> **MySQL 버퍼 풀 캐싱 효과**
>
> 인덱스 DROP 후 측정(cold) → 인덱스 CREATE 후 측정(warm) 순서로 진행되어,
> "개선 후" 결과에 버퍼 풀 캐싱 이점이 포함될 수 있습니다.
>
> 이를 완화하기 위해 **각 측정 단계 전 2회 워밍업 쿼리**를 실행하여 cold-cache 편향을
> 줄였습니다. 다만 206ms → 0ms 같은 극단적 차이의 일부는 캐싱 효과일 가능성이 있으며,
> EXPLAIN 결과(Full Table Scan vs Index Lookup)가 인덱스 효과의 본질적 증거입니다.

---

## Phase 4: 동시성 제어

### 4-1. ID 생성 충돌 해결

**개선 내용**: `System.currentTimeMillis()` 기반 ID 생성에서 Snowflake 기반 `DistributedIdGenerator`로 교체

**측정 방법**: 10 스레드 x 1,000 ID 동시 생성 비교

| 항목 | 개선 전 (currentTimeMillis) | 개선 후 (DistributedIdGenerator) |
|------|---------------------------|--------------------------------|
| 스레드 수 | 10 | 10 |
| 스레드당 ID | 1,000 | 1,000 |
| 총 시도 | 10,000 | 10,000 |
| 고유 ID 수 | **5** | **10,000** |
| 충돌 수 | **9,995 (99.95%)** | **0 (0.00%)** |
| 고유 ID 비율 | **0.05%** | **100%** |
| 소요 시간 | 6ms | 9ms |
| 처리량 | ~1,666,667 IDs/sec | ~1,111,111 IDs/sec |

**핵심 개선 사항**:
- **충돌률 99.95% -> 0.00%** (완전 해결)
- Snowflake 64비트 구조: 41비트 타임스탬프 + 10비트 워커ID + 12비트 시퀀스
- ms당 4,096개 ID 생성 가능 (워커당)
- 최대 1,024개 워커 인스턴스 지원
- `synchronized` 메서드로 스레드 안전성 보장
- 시계 역행 시 대기 처리

### 4-2. 낙관적 락 (@Version)

**개선 내용**: `CommonTimeEntity`에 `@Version` 필드 추가, `GlobalExceptionHandler`에서 충돌 처리

| 항목 | 개선 전 | 개선 후 |
|------|--------|--------|
| 동시 수정 감지 | 없음 (Last Writer Wins) | @Version 낙관적 락 |
| 충돌 응답 | 없음 | HTTP 409 Conflict |
| 적용 엔티티 | - | Board, Reply, BookReview, Category |
| 마이그레이션 | - | V3: `version BIGINT DEFAULT 0 NOT NULL` |

#### 한계점 및 주의사항

> **`synchronized`의 처리량 한계 (문제 6)**
>
> `DistributedIdGenerator`의 `generateBoardId()`, `generateReplyId()`, `nextId()` 모두
> `synchronized`로 선언되어 모든 스레드가 직렬화됩니다.
>
> 현재 측정된 처리량(~100만 IDs/sec)은 일반적인 웹 트래픽에 충분하지만,
> 초당 수천~수만 건의 동시 요청이 발생하는 고부하 환경에서는 병목이 될 수 있습니다.
>
> **대안:**
> - `AtomicLong` 기반 CAS (Compare-And-Swap) 연산
> - `ThreadLocal` 카운터 (스레드별 독립 시퀀스)
> - `LongAdder` 기반 분산 카운팅
>
> **인메모리 비교의 한계 (문제 7)**
>
> Phase 4 테스트는 **순수 인메모리** ID 생성 비교입니다. 실제 DB INSERT 시의 PK 충돌,
> unique constraint violation, 트랜잭션 롤백 등은 테스트되지 않았습니다.
> 실제 DB 레벨 동시성은 별도 통합 테스트로 검증이 필요합니다.
>
> **단일 인스턴스 테스트의 한계 (문제 8)**
>
> `workerId=0` 단일 인스턴스에서만 테스트했습니다. Snowflake 알고리즘의 핵심인
> 멀티 인스턴스 고유성(workerId 분리)은 이 테스트에서 검증되지 않았습니다.
> 멀티 인스턴스 환경에서는 각 인스턴스에 고유한 `workerId`를 할당하여
> (`WORKER_ID` 환경 변수) ID 고유성을 보장합니다.

---

## 종합 성능 개선 요약

| Phase | 개선 항목 | 개선 전 | 개선 후 | 개선율 | 측정 조건 |
|-------|---------|--------|--------|--------|----------|
| **1-1** | N+1 쿼리 (Reply + Member) | 1 + N 쿼리 (이론값) | 1 쿼리 (fetchJoin) | 쿼리 수 N개 감소 | batch_fetch_size=100 적용 환경. 실측 차이는 미미할 수 있음 |
| **1-2** | @Formula 서브쿼리 | 3.1ms (avg) | 0.5ms (avg) | **83.9%** | 10회 평균, 네이티브 쿼리 직접 비교 |
| **1-3** | @Version 충돌 시연 | - | - | - | like_cnt UPDATE → version 증가 → 동시 편집 실패 확인 |
| **2-1** | Reply 페이지네이션 (소량) | 11.6ms (전체 12개) | 25.8ms (12개, 1페이지) | -122.4% (악화) | 루트 Reply 12개, 페이지 크기 20 (소량 데이터에서 오버헤드) |
| **2-2** | Reply 페이지네이션 (대량) | 200개 전체 로드 | 20개 반환 | **90% 전송량 감소** | 200개 루트 Reply 동적 생성, 트랜잭션 롤백 |
| **3** | Board 인덱스 | 206ms (Full Scan, 195K rows) | < 1ms (Index, 1.5K rows) | **~100%** | FK_CHECKS=0으로 전체 DROP, 워밍업 2회 포함, 버퍼풀 캐싱 영향 가능 |
| **3** | Reply 인덱스 | 808ms (Full Scan, 1.29M rows) | 1ms (Index, 156 rows) | **99.9%** | 동일 |
| **3** | BookReview 인덱스 | 183ms (Full Scan, 195K rows) | 1ms (Index, 1.5K rows) | **99.5%** | 동일 |
| **4-1** | ID 생성 충돌 | 99.95% 충돌률 (5/10,000 고유) | 0% 충돌률 (10,000/10,000 고유) | **100%** | 인메모리 비교, 단일 인스턴스(workerId=0), synchronized 직렬화 |
| **4-2** | 동시성 제어 | Last Writer Wins | @Version 낙관적 락 + 409 응답 | 데이터 정합성 보장 | - |

---

## 테스트 실행 결과

### BeforeAfterComparisonTest (7 tests)

| 테스트 | 설명 |
|--------|------|
| Phase 1-1: N+1 Query fetchJoin Comparison | fetchJoin vs batch_fetch_size 쿼리 수 비교 + 이론값 |
| Phase 1-2: @Formula Subquery vs Stored Column | 서브쿼리 제거 성능 + @Version 트레이드오프 설명 |
| Phase 1-3: @Version Conflict Demonstration | like_cnt + 글 수정 동시 실행 시 충돌 시연 |
| Phase 2-1: Reply Pagination (기존 데이터) | 소량 데이터에서의 페이지네이션 한계 문서화 |
| Phase 2-2: Pagination Large Data Simulation | 200개 루트 Reply로 대량 데이터 페이지네이션 효과 검증 |
| Phase 3: Index DROP/CREATE Comparison | FK_CHECKS 비활성화 + 워밍업 쿼리 포함 |
| Phase 4: ID Generation Comparison | 처리량(IDs/sec) 메트릭 추가 + 한계점 문서화 |

### 기존 Performance Tests

| 테스트 클래스 | 테스트 수 | 주요 검증 항목 |
|-------------|----------|--------------|
| BoardQueryPerformanceTest | 7 | 쿼리 수 <= 2, fetchJoin, 스트레스 100회 |
| ReplyPaginationPerformanceTest | 8 | 전/후 비교, 깊이 제한, 배치 로딩, 중복 없음 |
| IndexPerformanceTest | 12 | 13개 인덱스 존재, EXPLAIN, FORCE INDEX |
| ConcurrencyPerformanceTest | 9 | 100K ID 유일성, 10스레드 0충돌, Snowflake |

---

## 측정 데이터 소스

- **테스트 결과 XML**: `build/test-results/test/TEST-com.booktalk_be.performance.comparison.BeforeAfterComparisonTest.xml`
- **Hibernate Statistics**: `SessionFactory.getStatistics()` (쿼리 실행 횟수, 엔티티 로드 수)
- **EXPLAIN**: MySQL 8.0 EXPLAIN 결과 (type, key, rows, Extra)
- **시간 측정**: `System.nanoTime()` 기반 밀리초 변환
- **워밍업**: 각 측정 단계 전 2회 워밍업 쿼리 실행 (cold-cache 편향 완화)
