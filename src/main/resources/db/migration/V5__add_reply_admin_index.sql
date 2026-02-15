-- Reply admin 쿼리 최적화: post_code 필터 + reply_code 정렬
-- 기존 idx_reply_postcode_del_regtime에서 post_code LIKE 'BO_%' 시 filtered=50% 문제 해결
-- 이 인덱스로 COUNT/데이터 쿼리 모두 정확한 범위 스캔 가능
CREATE INDEX idx_reply_postcode_replycode ON reply(post_code, reply_code DESC);
