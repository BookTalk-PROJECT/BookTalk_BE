DELIMITER //

DROP PROCEDURE IF EXISTS sp_mypage_gathering_board_search //

CREATE PROCEDURE sp_mypage_gathering_board_search(
    IN p_member_id INT,
    IN p_keyword_type VARCHAR(50),
    IN p_keyword VARCHAR(100),
    IN p_start_date VARCHAR(10),
    IN p_end_date VARCHAR(10),
    IN p_page_num INT,
    IN p_page_size INT
)
BEGIN
    DECLARE v_offset INT DEFAULT 0;
    DECLARE v_total BIGINT DEFAULT 0;
    DECLARE v_total_pages INT DEFAULT 0;

    SET v_offset = (p_page_num - 1) * p_page_size;

    SET p_keyword = NULLIF(TRIM(p_keyword), '');
    SET p_keyword_type = NULLIF(TRIM(p_keyword_type), '');
    SET p_start_date = NULLIF(TRIM(p_start_date), '');
    SET p_end_date = NULLIF(TRIM(p_end_date), '');

SELECT COUNT(*)
INTO v_total
FROM gathering_board gb
         JOIN gathering g ON g.gathering_code = gb.gathering_code
         JOIN member m ON m.member_id = gb.member_id
WHERE gb.member_id = p_member_id
  AND (
    p_keyword IS NULL
        OR (p_keyword_type = 'gathering_name' AND g.name LIKE CONCAT('%', p_keyword, '%'))
        OR (p_keyword_type = 'title' AND gb.title LIKE CONCAT('%', p_keyword, '%'))
        OR (p_keyword_type = 'author' AND m.name LIKE CONCAT('%', p_keyword, '%'))
    )
  AND (
    (p_start_date IS NULL AND p_end_date IS NULL)
        OR (DATE(gb.reg_time) BETWEEN
        IFNULL(STR_TO_DATE(p_start_date, '%Y-%m-%d'), '1000-01-01')
        AND IFNULL(STR_TO_DATE(p_end_date, '%Y-%m-%d'), '9999-12-31'))
    );

SET v_total_pages = IF(v_total = 0, 0, CEIL(v_total / p_page_size));

SELECT
    gb.code AS board_code,
    g.name AS gathering_name,
    gb.title AS title,
    m.name AS author,
    IFNULL(gb.del_yn, 0) AS del_yn,
    DATE_FORMAT(gb.reg_time, '%Y-%m-%d') AS reg_date,
    v_total_pages AS total_pages
FROM gathering_board gb
         JOIN gathering g ON g.gathering_code = gb.gathering_code
         JOIN member m ON m.member_id = gb.member_id
WHERE gb.member_id = p_member_id
  AND (
    p_keyword IS NULL
        OR (p_keyword_type = 'gathering_name' AND g.name LIKE CONCAT('%', p_keyword, '%'))
        OR (p_keyword_type = 'title' AND gb.title LIKE CONCAT('%', p_keyword, '%'))
        OR (p_keyword_type = 'author' AND m.name LIKE CONCAT('%', p_keyword, '%'))
    )
  AND (
    (p_start_date IS NULL AND p_end_date IS NULL)
        OR (DATE(gb.reg_time) BETWEEN
        IFNULL(STR_TO_DATE(p_start_date, '%Y-%m-%d'), '1000-01-01')
        AND IFNULL(STR_TO_DATE(p_end_date, '%Y-%m-%d'), '9999-12-31'))
    )
ORDER BY gb.reg_time DESC
    LIMIT p_page_size OFFSET v_offset;
END//

DELIMITER ;
