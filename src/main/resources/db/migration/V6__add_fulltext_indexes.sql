-- 관리자 검색 성능 최적화: MySQL FULLTEXT Index (ngram 파서)
-- 기존 LIKE '%keyword%' → MATCH...AGAINST 전환으로 풀 스캔 제거
-- ngram_token_size=2 (MySQL 기본값) → 한국어 2글자 토큰으로 분리

CREATE FULLTEXT INDEX ft_board_title ON board(title) WITH PARSER ngram;
CREATE FULLTEXT INDEX ft_reply_content ON reply(content) WITH PARSER ngram;
