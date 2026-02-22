# 관리자 검색 성능 개선 리포트

## 1. 문제 분석

### 현상
관리자 페이지의 키워드 검색(댓글 내용, 게시글 제목 등)이 매우 느림.

### 근본 원인

#### 원인 1: `%keyword%` 양방향 와일드카드 → 인덱스 사용 불가

모든 키워드 검색이 QueryDSL `containsIgnoreCase()` 메서드를 사용:
```sql
-- 생성되는 SQL
WHERE UPPER(board.title) LIKE UPPER('%테스트%')
WHERE UPPER(reply.content) LIKE UPPER('%테스트%')
```
`%keyword%` 패턴은 B-tree 인덱스의 정렬 순서를 활용할 수 없어 **풀 테이블 스캔** 발생.

#### 원인 2: `UPPER()` 함수 래핑 → 불필요 + 인덱스 차단

MySQL `utf8mb4_unicode_ci` collation은 **기본적으로 대소문자 무시**.
`containsIgnoreCase()`가 생성하는 `UPPER()` 함수 래핑은:
- 불필요한 함수 호출 (CI collation에서 LIKE는 이미 case-insensitive)
- 함수 인덱스가 아닌 일반 인덱스 사용 차단

#### 원인 3: COUNT 쿼리에 항상 `leftJoin(member)` 포함

`searchAllBoardsForPaging`, `searchAllRepliesForPaging`의 COUNT 쿼리가
AUTHOR 검색이 아닌 경우에도 항상 `leftJoin(member)`를 포함하여 불필요한 JOIN 발생.

### 영향 범위

| 테이블 | 실측 행 수 | 영향받는 검색 |
|--------|-----------|-------------|
| `board` | 1,000,003 | 제목(TITLE), 작성자(AUTHOR) |
| `reply` | 3,000,010 | 내용(CONTENT), 게시글코드(POST_CODE), 댓글코드(REPLY_CODE) |

---

## 2. 성능 측정 결과 — MySQL 직접 쿼리 (SET profiling)

> FULLTEXT 인덱스를 DROP/CREATE하여 **동일 데이터, 동일 환경**에서 측정.
> 애플리케이션 캐시(Caffeine)의 영향을 배제한 순수 DB 쿼리 실행 시간.

### 2-1. 개선 전 (FULLTEXT 인덱스 없음 + UPPER LIKE 패턴)

```sql
-- board TITLE (100만 행 풀 스캔)
SELECT count(*) FROM board WHERE UPPER(title) LIKE UPPER('%테스트%');
-- reply CONTENT (300만 행 풀 스캔)
SELECT count(*) FROM reply WHERE post_code LIKE 'BO\_%' AND UPPER(content) LIKE UPPER('%테스트%');
-- board AUTHOR (LEFT JOIN + UPPER LIKE)
SELECT count(*) FROM board b LEFT JOIN member m ON m.member_id = b.member_id WHERE UPPER(m.name) LIKE UPPER('%test%');
-- reply POST_CODE (UPPER LIKE)
SELECT count(*) FROM reply WHERE post_code LIKE 'BO\_%' AND UPPER(post_code) LIKE UPPER('%BO_%');
```

| 쿼리 | 1회차 | 2회차 |
|------|-------|-------|
| board TITLE `UPPER LIKE` | 3,075ms | 2,973ms |
| reply CONTENT `UPPER LIKE` | 7,197ms | 7,181ms |
| board AUTHOR `UPPER LIKE + JOIN` | 466ms | — |
| reply POST_CODE `UPPER LIKE` | 2,289ms | — |

### 2-2. 개선 후 (FULLTEXT 인덱스 + MATCH AGAINST / LIKE without UPPER)

```sql
-- board TITLE (FULLTEXT 인덱스 사용)
SELECT count(*) FROM board WHERE MATCH(title) AGAINST('테스트' IN BOOLEAN MODE);
-- reply CONTENT (FULLTEXT 인덱스 사용)
SELECT count(*) FROM reply WHERE post_code LIKE 'BO\_%' AND MATCH(content) AGAINST('테스트' IN BOOLEAN MODE);
-- board AUTHOR (UPPER 제거)
SELECT count(*) FROM board b LEFT JOIN member m ON m.member_id = b.member_id WHERE m.name LIKE '%test%';
-- reply POST_CODE (UPPER 제거)
SELECT count(*) FROM reply WHERE post_code LIKE 'BO\_%' AND post_code LIKE '%BO_%';
```

| 쿼리 | 1회차 | 2회차 |
|------|-------|-------|
| board TITLE `MATCH AGAINST` | 1.5ms | 0.28ms |
| reply CONTENT `MATCH AGAINST` | 6.9ms | 0.36ms |
| board AUTHOR `LIKE (no UPPER)` | 466ms | — |
| reply POST_CODE `LIKE (no UPPER)` | 2,009ms | — |

### 2-3. Before / After 비교

| 쿼리 | Before (2회차) | After (2회차) | 개선율 |
|------|---------------|--------------|--------|
| **board TITLE 검색** | 2,973ms | **0.28ms** | **10,618x** |
| **reply CONTENT 검색** | 7,181ms | **0.36ms** | **19,947x** |
| board AUTHOR 검색 | 466ms | 466ms | (변경 없음 — member 1만 행이라 LIKE로 충분) |
| reply POST_CODE 검색 | 2,289ms | 2,009ms | ~12% (UPPER 제거 효과) |

---

## 3. 성능 측정 결과 — MockMvc API 엔드포인트

> `SearchPerformanceTest` (warm-up 3회 + 측정 5회) 결과.
> Caffeine 캐시가 warm-up 이후 결과를 캐싱하므로 API 레벨에서는 차이가 미미.
> **캐시 미스(첫 요청) 시에는 위 SQL 직접 측정 결과와 동일한 지연 발생.**

### Before (FULLTEXT 인덱스 없음 + 원본 코드)

> CSV: `src/test/resources/performance/search_results_before_20260213_220153.csv`

| # | 검색 유형 | avg_ms | min_ms | max_ms |
|---|----------|--------|--------|--------|
| 1 | 게시글 제목 검색 (TITLE) | 3 | 3 | 3 |
| 2 | 게시글 작성자 검색 (AUTHOR) | 3 | 2 | 5 |
| 3 | 게시글 카테고리 검색 (CATEGORY) | 2 | 2 | 3 |
| 4 | 게시글 코드 검색 (CODE) | 2 | 2 | 3 |
| 5 | 댓글 내용 검색 (CONTENT, community) | 2 | 2 | 3 |
| 6 | 댓글 내용 검색 (CONTENT, bookreview) | 2 | 2 | 3 |
| 7 | 댓글 게시글코드 검색 (POST_CODE) | 2 | 2 | 3 |
| 8 | 댓글 내용+날짜 검색 (CONTENT+DATE) | 2 | 2 | 3 |

### After (FULLTEXT 인덱스 + 최적화 코드)

> CSV: `src/test/resources/performance/search_results_after_20260213_221309.csv`

| # | 검색 유형 | avg_ms | min_ms | max_ms |
|---|----------|--------|--------|--------|
| 1 | 게시글 제목 검색 (TITLE) | 4 | 4 | 5 |
| 2 | 게시글 작성자 검색 (AUTHOR) | 4 | 3 | 8 |
| 3 | 게시글 카테고리 검색 (CATEGORY) | 2 | 2 | 3 |
| 4 | 게시글 코드 검색 (CODE) | 3 | 3 | 3 |
| 5 | 댓글 내용 검색 (CONTENT, community) | 3 | 3 | 3 |
| 6 | 댓글 내용 검색 (CONTENT, bookreview) | 3 | 3 | 4 |
| 7 | 댓글 게시글코드 검색 (POST_CODE) | 2 | 2 | 2 |
| 8 | 댓글 내용+날짜 검색 (CONTENT+DATE) | 2 | 2 | 3 |

> MockMvc 테스트에서 before/after가 비슷한 이유:
> Caffeine 캐시가 warm-up 3회 동안 결과를 캐싱하여, 측정 5회는 캐시 히트(~2-4ms)만 측정됨.
> **실제 캐시 미스 시(첫 요청, 캐시 만료 후)에는 SQL 직접 측정 결과가 적용됨.**

---

## 4. 적용한 최적화

### 4-1. FULLTEXT 인덱스 추가 (V6 마이그레이션)

```sql
CREATE FULLTEXT INDEX ft_board_title ON board(title) WITH PARSER ngram;
CREATE FULLTEXT INDEX ft_reply_content ON reply(content) WITH PARSER ngram;
```

- **ngram 파서**: 한국어 텍스트를 2글자 토큰으로 분리 (ngram_token_size=2)
- `member.name`은 약 1만 행이므로 FULLTEXT 불필요 (B-tree `contains`로 충분)

### 4-2. Hibernate 커스텀 함수 등록

Hibernate 6 HQL 파서는 `MATCH...AGAINST` 구문을 인식하지 못하므로,
`FunctionContributor` SPI를 통해 `match_against` 커스텀 함수를 등록:

```java
// FullTextFunctionContributor.java
functionContributions.getFunctionRegistry()
    .patternDescriptorBuilder("match_against",
            "MATCH(?1) AGAINST(?2 IN BOOLEAN MODE)")
    .setInvariantType(...)
    .setExactArgumentCount(2)
    .register();
```

QueryDSL에서 사용:
```java
Expressions.numberTemplate(Double.class,
    "function('match_against', {0}, {1})", board.title, keyword).gt(0)
```

### 4-3. QueryDSL 검색 쿼리 변경

#### BoardRepositoryCustomImpl

| 검색 타입 | Before | After |
|----------|--------|-------|
| TITLE | `board.title.containsIgnoreCase(keyword)` | `function('match_against', board.title, keyword) > 0` |
| AUTHOR | `board.member.name.containsIgnoreCase(keyword)` | `board.member.name.contains(keyword)` |
| CATEGORY | 변경 없음 (서브쿼리 방식) | 변경 없음 |
| CODE | 변경 없음 (`eq`) | 변경 없음 |

#### ReplyRepositoryCustomImpl

| 검색 타입 | Before | After |
|----------|--------|-------|
| CONTENT | `reply.content.containsIgnoreCase(keyword)` | `function('match_against', reply.content, keyword) > 0` |
| POST_CODE | `reply.postCode.containsIgnoreCase(keyword)` | `reply.postCode.contains(keyword)` |
| REPLY_CODE | `reply.replyCode.containsIgnoreCase(keyword)` | `reply.replyCode.contains(keyword)` |

### 4-4. COUNT 쿼리 조건부 JOIN

#### BoardRepositoryCustomImpl.searchAllBoardsForPaging

```java
// Before: 항상 leftJoin(board.member)
JPAQueryBase<Long, JPAQuery<Long>> pageQuery =
    select(Wildcard.count).from(board).leftJoin(board.member);

// After: AUTHOR 검색일 때만 JOIN
JPAQuery<Long> pageQuery = select(Wildcard.count).from(board);
if (cmd.getType() == PostSearchCondCommand.KeywordType.AUTHOR) {
    pageQuery.leftJoin(board.member);
}
```

#### ReplyRepositoryCustomImpl.searchAllRepliesForPaging

```java
// Before: 항상 leftJoin(reply.member)
JPAQueryBase<Long, JPAQuery<Long>> pageQuery =
    select(Wildcard.count).from(reply).leftJoin(reply.member)
    .where(postCodePrefixFilter(postCodePrefix));

// After: member JOIN 제거 (Reply 검색에 AUTHOR 키워드 타입이 없으므로)
JPAQuery<Long> pageQuery = select(Wildcard.count).from(reply)
    .where(postCodePrefixFilter(postCodePrefix));
```

---

## 5. 제약 사항 및 주의점

### FULLTEXT + ngram 특성

1. **최소 토큰 길이**: `ngram_token_size=2` (MySQL 기본값)이므로 1글자 검색은 결과가 부정확할 수 있음
2. **인덱스 크기**: ngram FULLTEXT 인덱스는 일반 B-tree보다 디스크 사용량이 큼 (reply 300만 행 기준 수백 MB)
3. **인덱스 생성 시간**: `reply` 테이블 300만 행에서 FULLTEXT 인덱스 생성에 수 분 소요 가능 (Online DDL)
4. **BOOLEAN MODE**: `IN BOOLEAN MODE`는 자연어 모드보다 정확한 매칭을 제공하지만, 연산자(`+`, `-`, `*`)가 키워드에 포함되면 특수 동작 발생

### Hibernate 커스텀 함수

- Hibernate 6 HQL 파서는 MySQL 전용 `MATCH...AGAINST` 구문을 지원하지 않음
- `FunctionContributor` SPI를 통해 `match_against` 함수를 등록하여 해결
- SPI 등록 파일: `META-INF/services/org.hibernate.boot.model.FunctionContributor`

### contains vs containsIgnoreCase

- MySQL `utf8mb4_unicode_ci` collation은 기본적으로 대소문자를 무시
- `contains()`는 `LIKE '%keyword%'`를 생성 (UPPER 래핑 없음)
- `containsIgnoreCase()`는 `UPPER(col) LIKE UPPER('%keyword%')`를 생성 (불필요한 함수 호출)
- POST_CODE, REPLY_CODE 같은 코드 값은 대소문자 구분이 불필요하므로 `contains`로 충분

### 조건부 JOIN

- AUTHOR 검색이 아닌 경우 COUNT 쿼리에서 member JOIN을 제거하여 불필요한 테이블 접근 방지
- AUTHOR 검색 시에는 반드시 member JOIN이 필요하므로 조건부로 추가

---

## 6. 수정 파일 요약

| 파일 | 작업 |
|------|------|
| `V6__add_fulltext_indexes.sql` | 신규 — FULLTEXT 인덱스 (ngram) |
| `FullTextFunctionContributor.java` | 신규 — Hibernate 커스텀 함수 등록 |
| `org.hibernate.boot.model.FunctionContributor` | 신규 — SPI 서비스 등록 파일 |
| `BoardRepositoryCustomImpl.java` | 수정 — MATCH AGAINST + 조건부 JOIN + contains |
| `ReplyRepositoryCustomImpl.java` | 수정 — MATCH AGAINST + JOIN 제거 + contains |
| `SearchPerformanceTest.java` | 신규 — 검색 성능 테스트 (8 케이스) |
| `build.gradle` | 수정 — test.phase 시스템 프로퍼티 전달 |
| `SEARCH_PERFORMANCE_REPORT.md` | 신규 — 본 리포트 |

---

## 7. 검증 방법

```bash
# 개선 전 측정 (코드 변경 전에 실행)
./gradlew test --tests "com.booktalk_be.performance.SearchPerformanceTest" -Dtest.phase=before

# 개선 후 측정 (코드 변경 + Flyway 마이그레이션 적용 후)
./gradlew test --tests "com.booktalk_be.performance.SearchPerformanceTest" -Dtest.phase=after
```
