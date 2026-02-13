-- =====================================================
-- BookTalk Database Schema Initialization
-- V1__init_schema.sql
-- =====================================================

-- =====================================================
-- 1. MEMBER TABLE (핵심 테이블)
-- =====================================================
CREATE TABLE IF NOT EXISTS member (
    member_id INT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    auth_type VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    address VARCHAR(255),
    gender VARCHAR(255),
    birth DATE,
    authority VARCHAR(50),
    del_yn BOOLEAN NOT NULL DEFAULT FALSE,
    reg_time DATETIME(6) NOT NULL,
    update_time DATETIME(6),
    version BIGINT,
    PRIMARY KEY (member_id),
    CONSTRAINT uk_member_email_auth_type UNIQUE (email, auth_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 2. CATEGORY TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS category (
    category_id INT NOT NULL AUTO_INCREMENT,
    value VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    p_category_id INT,
    del_yn BOOLEAN NOT NULL DEFAULT FALSE,
    reg_time DATETIME(6) NOT NULL,
    update_time DATETIME(6),
    version BIGINT,
    PRIMARY KEY (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 3. HASH_TAG TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS hash_tag (
    hashtag_id BIGINT NOT NULL AUTO_INCREMENT,
    value VARCHAR(255) NOT NULL,
    PRIMARY KEY (hashtag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 4. RECRUIT_QUESTION TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS recruit_question (
    recruit_question BIGINT NOT NULL AUTO_INCREMENT,
    question_order INT NOT NULL,
    question VARCHAR(255) NOT NULL,
    PRIMARY KEY (recruit_question)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 5. GATHERING TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS gathering (
    gathering_code VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    name VARCHAR(255),
    recruitment_personnel BIGINT NOT NULL,
    recruitment_period VARCHAR(255) NOT NULL,
    activity_period VARCHAR(255) NOT NULL,
    emd_cd VARCHAR(255),
    sig_cd VARCHAR(255),
    image_url VARCHAR(255),
    summary VARCHAR(255) NOT NULL,
    del_yn BOOLEAN NOT NULL DEFAULT FALSE,
    del_reason VARCHAR(255),
    created_by VARCHAR(255) NOT NULL,
    modified_by VARCHAR(255),
    reg_time DATETIME(6) NOT NULL,
    update_time DATETIME(6),
    version BIGINT,
    PRIMARY KEY (gathering_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 6. BOARD TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS board (
    code VARCHAR(255) NOT NULL,
    member_id INT,
    category_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    views INT DEFAULT 0,
    like_cnt INT DEFAULT 0,
    del_yn BOOLEAN NOT NULL,
    notification_yn BOOLEAN NOT NULL,
    del_reason VARCHAR(255),
    created_by VARCHAR(255) NOT NULL,
    modified_by VARCHAR(255),
    reg_time DATETIME(6) NOT NULL,
    update_time DATETIME(6),
    version BIGINT,
    PRIMARY KEY (code),
    CONSTRAINT fk_board_member FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 7. BOOK_REVIEW TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS book_review (
    code VARCHAR(255) NOT NULL,
    member_id INT,
    category_id INT NOT NULL,
    book_title VARCHAR(255) NOT NULL,
    authors VARCHAR(255) NOT NULL,
    publisher VARCHAR(255) NOT NULL,
    isbn VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(5000),
    rating INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    views INT DEFAULT 0,
    like_cnt INT DEFAULT 0,
    del_yn BOOLEAN NOT NULL,
    notification_yn BOOLEAN NOT NULL DEFAULT FALSE,
    del_reason VARCHAR(255),
    created_by VARCHAR(255) NOT NULL,
    modified_by VARCHAR(255),
    reg_time DATETIME(6) NOT NULL,
    update_time DATETIME(6),
    version BIGINT,
    PRIMARY KEY (code),
    CONSTRAINT fk_book_review_member FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 8. REPLY TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS reply (
    reply_code VARCHAR(255) NOT NULL,
    member_id INT,
    post_code VARCHAR(255) NOT NULL,
    parent_reply_code VARCHAR(255),
    content TEXT NOT NULL,
    like_cnt INT NOT NULL DEFAULT 0,
    del_yn BOOLEAN NOT NULL,
    del_reason VARCHAR(255),
    created_by VARCHAR(255) NOT NULL,
    modified_by VARCHAR(255),
    reg_time DATETIME(6) NOT NULL,
    update_time DATETIME(6),
    version BIGINT,
    PRIMARY KEY (reply_code),
    CONSTRAINT fk_reply_member FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT fk_reply_parent FOREIGN KEY (parent_reply_code) REFERENCES reply(reply_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 9. LIKES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS likes (
    code VARCHAR(255) NOT NULL,
    member_id INT NOT NULL,
    reg_time DATETIME(6) NOT NULL,
    update_time DATETIME(6),
    version BIGINT,
    PRIMARY KEY (code, member_id),
    CONSTRAINT fk_likes_member FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 10. REFRESH_TOKEN TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS refresh_token (
    member_id INT NOT NULL,
    refresh_token VARCHAR(255) NOT NULL,
    PRIMARY KEY (member_id, refresh_token),
    CONSTRAINT fk_refresh_token_member FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 11. GATHERING_BOARD TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS gathering_board (
    code VARCHAR(255) NOT NULL,
    gathering_code VARCHAR(255),
    member_id INT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    views INT DEFAULT 0,
    like_cnt INT DEFAULT 0,
    del_yn BOOLEAN NOT NULL,
    notification_yn BOOLEAN NOT NULL DEFAULT FALSE,
    del_reason VARCHAR(255),
    created_by VARCHAR(255) NOT NULL,
    modified_by VARCHAR(255),
    reg_time DATETIME(6) NOT NULL,
    update_time DATETIME(6),
    version BIGINT,
    PRIMARY KEY (code),
    CONSTRAINT fk_gathering_board_gathering FOREIGN KEY (gathering_code) REFERENCES gathering(gathering_code),
    CONSTRAINT fk_gathering_board_member FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 12. GATHERING_MEMBER_MAP TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS gathering_member_map (
    gathering_code VARCHAR(255) NOT NULL,
    member_id INT NOT NULL,
    master_yn BOOLEAN,
    PRIMARY KEY (gathering_code, member_id),
    CONSTRAINT fk_gathering_member_map_gathering FOREIGN KEY (gathering_code) REFERENCES gathering(gathering_code),
    CONSTRAINT fk_gathering_member_map_member FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 13. GATHERING_BOOK_MAP TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS gathering_book_map (
    gathering_code VARCHAR(255) NOT NULL,
    isbn VARCHAR(255) NOT NULL,
    book_name VARCHAR(255) NOT NULL,
    orders INT NOT NULL,
    complete_yn BOOLEAN NOT NULL,
    start_date VARCHAR(255) NOT NULL,
    end_date VARCHAR(255),
    PRIMARY KEY (gathering_code, isbn),
    CONSTRAINT fk_gathering_book_map_gathering FOREIGN KEY (gathering_code) REFERENCES gathering(gathering_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 14. HASH_TAG_MAP TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS hash_tag_map (
    gathering_code VARCHAR(255) NOT NULL,
    hashtag_id BIGINT NOT NULL,
    PRIMARY KEY (gathering_code, hashtag_id),
    CONSTRAINT fk_hash_tag_map_gathering FOREIGN KEY (gathering_code) REFERENCES gathering(gathering_code),
    CONSTRAINT fk_hash_tag_map_hashtag FOREIGN KEY (hashtag_id) REFERENCES hash_tag(hashtag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 15. GATHERING_RECRUIT_QUESTION_MAP TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS gathering_recruit_question_map (
    gathering_code VARCHAR(255) NOT NULL,
    recruit_question BIGINT NOT NULL,
    PRIMARY KEY (gathering_code, recruit_question),
    CONSTRAINT fk_gathering_recruit_question_map_gathering FOREIGN KEY (gathering_code) REFERENCES gathering(gathering_code),
    CONSTRAINT fk_gathering_recruit_question_map_question FOREIGN KEY (recruit_question) REFERENCES recruit_question(recruit_question)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- 16. RECRUIT_REQUEST TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS recruit_request (
    gathering_code VARCHAR(255) NOT NULL,
    member_id INT NOT NULL,
    recruit_question VARCHAR(100) NOT NULL,
    request_question_answer VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    reject_reason VARCHAR(255),
    PRIMARY KEY (gathering_code, member_id, recruit_question),
    CONSTRAINT fk_recruit_request_gathering FOREIGN KEY (gathering_code) REFERENCES gathering(gathering_code),
    CONSTRAINT fk_recruit_request_member FOREIGN KEY (member_id) REFERENCES member(member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
