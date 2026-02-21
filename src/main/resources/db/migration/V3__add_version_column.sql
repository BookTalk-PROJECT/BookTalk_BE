-- Add version column for Optimistic Locking
-- Enables concurrent update detection and prevents lost updates
-- Compatible with MySQL 8.0+

-- =====================================================
-- BOARD TABLE
-- =====================================================
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'board' AND COLUMN_NAME = 'version');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE board ADD COLUMN version BIGINT DEFAULT 0 NOT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- REPLY TABLE
-- =====================================================
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reply' AND COLUMN_NAME = 'version');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE reply ADD COLUMN version BIGINT DEFAULT 0 NOT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- BOOK_REVIEW TABLE
-- =====================================================
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book_review' AND COLUMN_NAME = 'version');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE book_review ADD COLUMN version BIGINT DEFAULT 0 NOT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- CATEGORY TABLE
-- =====================================================
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'category' AND COLUMN_NAME = 'version');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE category ADD COLUMN version BIGINT DEFAULT 0 NOT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
