DELIMITER //

DROP PROCEDURE IF EXISTS sp_mypage_gathering_search //

CREATE PROCEDURE sp_mypage_gathering_search(
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

    -- total count
SELECT COUNT(*)
INTO v_total
FROM gathering_member_map gmm
         JOIN gathering g ON g.gathering_code = gmm.gathering_code
WHERE gmm.member_id = p_member_id
  AND (
    p_keyword IS NULL
        OR (
        p_keyword_type = 'gathering_code' AND g.gathering_code LIKE CONCAT('%', p_keyword, '%')
        )
        OR (
        p_keyword_type = 'name' AND g.name LIKE CONCAT('%', p_keyword, '%')
        )
    )
  AND (
    (p_start_date IS NULL AND p_end_date IS NULL)
        OR (DATE(g.reg_time) BETWEEN
        IFNULL(STR_TO_DATE(p_start_date, '%Y-%m-%d'), '1000-01-01')
        AND IFNULL(STR_TO_DATE(p_end_date, '%Y-%m-%d'), '9999-12-31'))
    );

SET v_total_pages = IF(v_total = 0, 0, CEIL(v_total / p_page_size));

    -- content
SELECT
    g.gathering_code AS gathering_code,
    g.name AS name,
    leader.name AS leader_name,
    IFNULL(gmm.master_yn, 0) AS master_yn,
    IFNULL(g.del_yn, 0) AS del_yn,
    DATE_FORMAT(g.reg_time, '%Y-%m-%d') AS reg_date,
    v_total_pages AS total_pages
FROM gathering_member_map gmm
         JOIN gathering g ON g.gathering_code = gmm.gathering_code

         LEFT JOIN gathering_member_map gmm_leader
                   ON gmm_leader.gathering_code = g.gathering_code
                       AND gmm_leader.master_yn = 1
         LEFT JOIN member leader
                   ON leader.member_id = gmm_leader.member_id

WHERE gmm.member_id = p_member_id
  AND (
    p_keyword IS NULL
        OR (
        p_keyword_type = 'gathering_code' AND g.gathering_code LIKE CONCAT('%', p_keyword, '%')
        )
        OR (
        p_keyword_type = 'name' AND g.name LIKE CONCAT('%', p_keyword, '%')
        )
    )
  AND (
    (p_start_date IS NULL AND p_end_date IS NULL)
        OR (DATE(g.reg_time) BETWEEN
        IFNULL(STR_TO_DATE(p_start_date, '%Y-%m-%d'), '1000-01-01')
        AND IFNULL(STR_TO_DATE(p_end_date, '%Y-%m-%d'), '9999-12-31'))
    )
ORDER BY g.reg_time DESC
    LIMIT p_page_size OFFSET v_offset;
END//

DELIMITER ;
