DELIMITER //

DROP PROCEDURE IF EXISTS sp_mypage_gathering_board_list //

CREATE PROCEDURE sp_mypage_gathering_board_list(
    IN p_member_id INT,
    IN p_page_num INT,
    IN p_page_size INT
)
BEGIN
    DECLARE v_offset INT DEFAULT 0;
    DECLARE v_total BIGINT DEFAULT 0;
    DECLARE v_total_pages INT DEFAULT 0;

    SET v_offset = (p_page_num - 1) * p_page_size;

SELECT COUNT(*)
INTO v_total
FROM gathering_board gb
         JOIN gathering g ON g.gathering_code = gb.gathering_code
         JOIN member m ON m.member_id = gb.member_id
WHERE gb.member_id = p_member_id;

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
ORDER BY gb.reg_time DESC
    LIMIT p_page_size OFFSET v_offset;
END//

DELIMITER ;
