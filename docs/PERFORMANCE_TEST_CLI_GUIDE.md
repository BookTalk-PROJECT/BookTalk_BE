# BookTalk Backend Performance Test CLI Guide

이 문서는 BookTalk Backend 성능 테스트 실행을 위한 CLI 가이드입니다.

## 목차

1. [사전 요구사항](#1-사전-요구사항)
2. [사전 설정 (Pre-Test)](#2-사전-설정-pre-test)
3. [더미 데이터 생성](#3-더미-데이터-생성)
4. [통합 테스트 실행](#4-통합-테스트-실행)
5. [k6 부하 테스트 실행](#5-k6-부하-테스트-실행)
6. [사후 검증 (Post-Test)](#6-사후-검증-post-test)
7. [트러블슈팅](#7-트러블슈팅)

---

## 1. 사전 요구사항

### 1.1 필수 소프트웨어

```bash
# Java 17+
java -version

# Gradle (또는 gradlew 사용)
./gradlew --version

# MySQL 8.0+
mysql --version

# k6 (부하 테스트용)
k6 version
# 설치: https://k6.io/docs/get-started/installation/
```

### 1.2 k6 설치

**Windows (Chocolatey):**
```powershell
choco install k6
```

**Windows (MSI 설치):**
```powershell
# https://dl.k6.io/msi/k6-latest-amd64.msi 다운로드 후 설치
```

**Mac:**
```bash
brew install k6
```

**Linux:**
```bash
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

---

## 2. 사전 설정 (Pre-Test)

### 2.1 데이터베이스 설정 확인

```bash
# MySQL 접속
mysql -u root -p booktalk

# 현재 인덱스 확인
SELECT INDEX_NAME, TABLE_NAME
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'booktalk' AND INDEX_NAME LIKE 'idx_%'
ORDER BY TABLE_NAME;

# 테이블별 레코드 수 확인
SELECT 'member' as tbl, COUNT(*) as cnt FROM member
UNION ALL SELECT 'category', COUNT(*) FROM category
UNION ALL SELECT 'board', COUNT(*) FROM board
UNION ALL SELECT 'book_review', COUNT(*) FROM book_review
UNION ALL SELECT 'reply', COUNT(*) FROM reply
UNION ALL SELECT 'likes', COUNT(*) FROM likes;
```

### 2.2 Flyway 마이그레이션 실행

```bash
# 마이그레이션 상태 확인
./gradlew flywayInfo

# 마이그레이션 실행
./gradlew flywayMigrate

# 특정 프로파일로 실행
./gradlew flywayMigrate -Dspring.profiles.active=dev
```

### 2.3 수동 인덱스 생성 (필요시)

```bash
# V2 인덱스 마이그레이션 수동 실행
mysql -u root -p booktalk < src/main/resources/db/migration/V2__add_performance_indexes.sql

# V3 버전 컬럼 마이그레이션 수동 실행
mysql -u root -p booktalk < src/main/resources/db/migration/V3__add_version_column.sql
```

### 2.4 애플리케이션 설정 확인

```yaml
# application.yml에서 확인할 설정
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100  # 배치 페치 사이즈
        generate_statistics: true       # 통계 활성화 (테스트용)

app:
  worker-id: ${WORKER_ID:0}  # 분산 ID 생성기 워커 ID
```

---

## 3. 더미 데이터 생성

### 3.1 전체 데이터 생성 (순차 실행)

```bash
# 전체 테스트 클래스 실행 (순차적으로 실행됨)
./gradlew test --tests "*.DummyDataGeneratorTest" -Dspring.profiles.active=dev
```

### 3.2 단계별 데이터 생성 (권장)

대용량 데이터 생성 시 단계별 실행 권장:

```bash
# 1단계: Member 1,000건
./gradlew test --tests "*.DummyDataGeneratorTest.generateMembers" -Dspring.profiles.active=dev

# 2단계: Category 100건
./gradlew test --tests "*.DummyDataGeneratorTest.generateCategories" -Dspring.profiles.active=dev

# 3단계: Board 100,000건
./gradlew test --tests "*.DummyDataGeneratorTest.generateBoards" -Dspring.profiles.active=dev

# 4단계: BookReview 100,000건
./gradlew test --tests "*.DummyDataGeneratorTest.generateBookReviews" -Dspring.profiles.active=dev

# 5단계: Root Reply 200,000건
./gradlew test --tests "*.DummyDataGeneratorTest.generateRootReplies" -Dspring.profiles.active=dev

# 6단계: Depth-1 Reply 300,000건
./gradlew test --tests "*.DummyDataGeneratorTest.generateDepth1Replies" -Dspring.profiles.active=dev

# 7단계: Depth-2 Reply 200,000건
./gradlew test --tests "*.DummyDataGeneratorTest.generateDepth2Replies" -Dspring.profiles.active=dev

# 8단계: Likes 100,000건
./gradlew test --tests "*.DummyDataGeneratorTest.generateLikes" -Dspring.profiles.active=dev

# 9단계: 데이터 검증
./gradlew test --tests "*.DummyDataGeneratorTest.verifyDataCounts" -Dspring.profiles.active=dev
```

### 3.3 데이터 생성 모니터링

```bash
# 별도 터미널에서 MySQL 모니터링
watch -n 5 "mysql -u root -p -e 'SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES WHERE TABLE_SCHEMA = \"booktalk\"'"
```

---

## 4. 통합 테스트 실행

### 4.1 N+1 쿼리 검증 테스트

```bash
# Board 쿼리 성능 테스트
./gradlew test --tests "*.BoardQueryPerformanceTest" -Dspring.profiles.active=dev

# 특정 테스트만 실행
./gradlew test --tests "*.BoardQueryPerformanceTest.testBoardListQueryCount" -Dspring.profiles.active=dev
./gradlew test --tests "*.BoardQueryPerformanceTest.testMemberFetchJoinWorking" -Dspring.profiles.active=dev
./gradlew test --tests "*.BoardQueryPerformanceTest.testMultipleBoardListRequests" -Dspring.profiles.active=dev
```

### 4.2 Reply 페이지네이션 검증 테스트

```bash
# Reply 페이지네이션 테스트
./gradlew test --tests "*.ReplyPaginationPerformanceTest" -Dspring.profiles.active=dev

# 특정 테스트만 실행
./gradlew test --tests "*.ReplyPaginationPerformanceTest.compareOldVsNewMethod" -Dspring.profiles.active=dev
./gradlew test --tests "*.ReplyPaginationPerformanceTest.testDepthLimitEnforcement" -Dspring.profiles.active=dev
./gradlew test --tests "*.ReplyPaginationPerformanceTest.testMultiplePagesLoad" -Dspring.profiles.active=dev
```

### 4.3 인덱스 검증 테스트

```bash
# 인덱스 성능 테스트
./gradlew test --tests "*.IndexPerformanceTest" -Dspring.profiles.active=dev

# 특정 테스트만 실행
./gradlew test --tests "*.IndexPerformanceTest.testAllIndexesExist" -Dspring.profiles.active=dev
./gradlew test --tests "*.IndexPerformanceTest.testBoardCategoryIndex" -Dspring.profiles.active=dev
./gradlew test --tests "*.IndexPerformanceTest.testQueryPerformanceWithIndexHints" -Dspring.profiles.active=dev
```

### 4.4 동시성 검증 테스트

```bash
# 동시성 테스트
./gradlew test --tests "*.ConcurrencyPerformanceTest" -Dspring.profiles.active=dev

# 특정 테스트만 실행
./gradlew test --tests "*.ConcurrencyPerformanceTest.testIdUniqueness_SingleThread" -Dspring.profiles.active=dev
./gradlew test --tests "*.ConcurrencyPerformanceTest.testIdUniqueness_MultiThreaded" -Dspring.profiles.active=dev
./gradlew test --tests "*.ConcurrencyPerformanceTest.testHighFrequencyIdGeneration" -Dspring.profiles.active=dev
```

### 4.5 전체 성능 테스트 실행

```bash
# 모든 성능 테스트 실행
./gradlew test --tests "com.booktalk_be.performance.*" -Dspring.profiles.active=dev

# 테스트 결과 리포트 생성
./gradlew test --tests "com.booktalk_be.performance.*" -Dspring.profiles.active=dev --info
```

---

## 5. k6 부하 테스트 실행

### 5.1 애플리케이션 시작

```bash
# 애플리케이션 실행 (별도 터미널)
./gradlew bootRun -Dspring.profiles.active=dev

# 또는 JAR 실행
java -jar -Dspring.profiles.active=dev build/libs/booktalk-be-*.jar
```

### 5.2 개별 부하 테스트 실행

```bash
# Board List 부하 테스트
k6 run k6/scripts/board-list-load-test.js

# Board Detail 부하 테스트
k6 run k6/scripts/board-detail-load-test.js

# Reply 페이지네이션 부하 테스트
k6 run k6/scripts/reply-pagination-load-test.js

# Category List 부하 테스트
k6 run k6/scripts/category-list-load-test.js

# 혼합 워크로드 부하 테스트
k6 run k6/scripts/mixed-workload-test.js
```

### 5.3 커스텀 옵션으로 실행

```bash
# 다른 서버 URL 지정
k6 run --env BASE_URL=http://192.168.1.100:8080 k6/scripts/board-list-load-test.js

# 다른 API prefix 지정
k6 run --env BASE_URL=http://localhost:8080 --env API_PREFIX=/community k6/scripts/board-list-load-test.js

# VU 수 오버라이드
k6 run --vus 200 --duration 5m k6/scripts/board-list-load-test.js
```

### 5.4 결과 저장

```bash
# JSON 형식으로 결과 저장
k6 run --out json=k6/results/board-list-$(date +%Y%m%d_%H%M%S).json k6/scripts/board-list-load-test.js

# CSV 형식으로 결과 저장
k6 run --out csv=k6/results/board-list-$(date +%Y%m%d_%H%M%S).csv k6/scripts/board-list-load-test.js

# 여러 출력 형식 동시 사용
k6 run --out json=k6/results/mixed-workload.json --out csv=k6/results/mixed-workload.csv k6/scripts/mixed-workload-test.js
```

### 5.5 테스트 결과 해석

k6 실행 후 출력되는 주요 메트릭:

| 메트릭 | 설명 | 목표 |
|--------|------|------|
| `http_req_duration` | HTTP 요청 지속 시간 | p95 < 1000ms |
| `http_req_failed` | 실패한 요청 비율 | rate < 5% |
| `http_reqs` | 초당 요청 수 | 높을수록 좋음 |
| `vus` | 활성 가상 사용자 수 | 설정값 확인 |
| `iteration_duration` | 단일 반복 시간 | 안정적이어야 함 |

---

## 6. 사후 검증 (Post-Test)

### 6.1 인덱스 사용 통계 확인

```sql
-- 인덱스 사용 통계 조회
SELECT
    OBJECT_NAME as table_name,
    INDEX_NAME as index_name,
    COUNT_STAR as accesses,
    COUNT_READ as reads,
    COUNT_WRITE as writes
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE OBJECT_SCHEMA = 'booktalk'
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY COUNT_STAR DESC;
```

### 6.2 EXPLAIN ANALYZE 확인

```sql
-- Board 카테고리 조회 쿼리 분석
EXPLAIN ANALYZE
SELECT * FROM board
WHERE category_id = 1 AND del_yn = false
ORDER BY reg_time DESC
LIMIT 20;

-- Reply 조회 쿼리 분석
EXPLAIN ANALYZE
SELECT * FROM reply
WHERE post_code = 'BO_1' AND del_yn = false
ORDER BY reg_time ASC
LIMIT 50;
```

### 6.3 데이터 건수 검증

```sql
-- 최종 데이터 건수 확인
SELECT 'member' as table_name, COUNT(*) as count FROM member
UNION ALL SELECT 'category', COUNT(*) FROM category
UNION ALL SELECT 'board', COUNT(*) FROM board
UNION ALL SELECT 'book_review', COUNT(*) FROM book_review
UNION ALL SELECT 'reply', COUNT(*) FROM reply
UNION ALL SELECT 'likes', COUNT(*) FROM likes;

-- 예상 총 건수: 약 1,101,100건
```

### 6.4 성능 지표 확인

```sql
-- 슬로우 쿼리 로그 확인 (MySQL)
SHOW VARIABLES LIKE 'slow_query%';
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 20;

-- InnoDB 버퍼 풀 히트율
SHOW STATUS LIKE 'Innodb_buffer_pool%';
```

---

## 7. 트러블슈팅

### 7.1 테스트 실패 시

**OutOfMemoryError 발생 시:**
```bash
# JVM 힙 메모리 증가
./gradlew test --tests "*.DummyDataGeneratorTest" -Dspring.profiles.active=dev -Dorg.gradle.jvmargs="-Xmx4g"
```

**테스트 타임아웃 시:**
```bash
# 테스트 타임아웃 증가
./gradlew test --tests "*.DummyDataGeneratorTest" -Dspring.profiles.active=dev --info -Dtest.timeout=3600
```

**데이터베이스 연결 실패 시:**
```bash
# MySQL 서버 상태 확인
systemctl status mysql
# 또는
brew services list | grep mysql

# 연결 테스트
mysql -u root -p -e "SELECT 1"
```

### 7.2 k6 부하 테스트 문제

**Connection refused 오류:**
```bash
# 애플리케이션 실행 확인
curl http://localhost:8080/actuator/health

# 포트 확인
netstat -an | grep 8080
```

**높은 에러율:**
```bash
# 서버 로그 확인
tail -f logs/application.log

# 커넥션 풀 상태 확인
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

### 7.3 인덱스 관련 문제

**인덱스가 사용되지 않는 경우:**
```sql
-- 인덱스 힌트 강제 사용
SELECT * FROM board FORCE INDEX (idx_board_category_del_regtime)
WHERE category_id = 1 AND del_yn = false
ORDER BY reg_time DESC LIMIT 20;

-- 인덱스 통계 업데이트
ANALYZE TABLE board;
ANALYZE TABLE reply;
ANALYZE TABLE book_review;
```

### 7.4 데이터 정리

**테스트 데이터 삭제:**
```sql
-- 주의: 모든 데이터가 삭제됩니다!
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE likes;
TRUNCATE TABLE reply;
TRUNCATE TABLE board;
TRUNCATE TABLE book_review;
TRUNCATE TABLE category;
TRUNCATE TABLE member;
SET FOREIGN_KEY_CHECKS = 1;
```

---

## 부록: 성능 목표 체크리스트

### Phase 1: N+1 쿼리 해결
- [ ] Board 목록 조회 시 쿼리 수 ≤ 2
- [ ] Board 상세 + 댓글 조회 시 쿼리 수 ≤ 5
- [ ] Member fetchJoin 동작 확인
- [ ] likesCnt 서브쿼리 제거 확인

### Phase 2: Reply 페이지네이션
- [ ] 페이지네이션 응답 시간 < 300ms
- [ ] Depth 3 제한 동작 확인
- [ ] 대용량 댓글에서 OOM 없음

### Phase 3: 인덱스 최적화
- [ ] 13개 인덱스 모두 생성 확인
- [ ] EXPLAIN에서 인덱스 사용 확인
- [ ] Full Table Scan 제거 확인

### Phase 4: 동시성 제어
- [ ] ID 고유성 100% 확인 (10만 건)
- [ ] 멀티스레드 ID 충돌 0건
- [ ] Optimistic Locking 동작 확인

### k6 부하 테스트
- [ ] Board List p95 < 500ms
- [ ] Board Detail p95 < 1000ms
- [ ] Reply List p95 < 800ms
- [ ] Category List p95 < 200ms
- [ ] Mixed Workload p95 < 1000ms
- [ ] 에러율 < 5%

---

**문서 버전**: 1.0.0
**최종 수정**: 2026-02-03
