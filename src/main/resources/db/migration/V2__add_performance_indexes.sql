-- Performance Indexes Migration
-- Created for BookTalk Backend Performance Improvement
-- Compatible with MySQL 8.0+ and Flyway (no DELIMITER changes)
-- Idempotent: checks INFORMATION_SCHEMA before creating each index

-- =====================================================
-- BOARD TABLE INDEXES
-- =====================================================

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'board' AND INDEX_NAME = 'idx_board_category_del_regtime');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_board_category_del_regtime ON board (category_id, del_yn, reg_time DESC)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'board' AND INDEX_NAME = 'idx_board_member_del');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_board_member_del ON board (member_id, del_yn)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'board' AND INDEX_NAME = 'idx_board_code_category_del');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_board_code_category_del ON board (code, category_id, del_yn)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- REPLY TABLE INDEXES
-- =====================================================

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reply' AND INDEX_NAME = 'idx_reply_postcode_del_regtime');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_reply_postcode_del_regtime ON reply (post_code, del_yn, reg_time ASC)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reply' AND INDEX_NAME = 'idx_reply_parent_del');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_reply_parent_del ON reply (parent_reply_code, del_yn)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reply' AND INDEX_NAME = 'idx_reply_member_del');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_reply_member_del ON reply (member_id, del_yn)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- CATEGORY TABLE INDEXES
-- =====================================================

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'category' AND INDEX_NAME = 'idx_category_active_del');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_category_active_del ON category (is_active, del_yn)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'category' AND INDEX_NAME = 'idx_category_parent');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_category_parent ON category (p_category_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- LIKES TABLE INDEXES
-- =====================================================

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'likes' AND INDEX_NAME = 'idx_likes_code');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_likes_code ON likes (code)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'likes' AND INDEX_NAME = 'idx_likes_member_code');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_likes_member_code ON likes (member_id, code)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- BOOK_REVIEW TABLE INDEXES
-- =====================================================

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book_review' AND INDEX_NAME = 'idx_bookreview_category_del_regtime');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_bookreview_category_del_regtime ON book_review (category_id, del_yn, reg_time DESC)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book_review' AND INDEX_NAME = 'idx_bookreview_member_del');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_bookreview_member_del ON book_review (member_id, del_yn)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book_review' AND INDEX_NAME = 'idx_bookreview_isbn');
SET @sql = IF(@idx = 0, 'CREATE INDEX idx_bookreview_isbn ON book_review (isbn)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
