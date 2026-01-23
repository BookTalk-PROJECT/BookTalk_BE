DELIMITER //

DROP PROCEDURE IF EXISTS sp_mypage_gathering_list //

CREATE PROCEDURE sp_mypage_gathering_list(
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
FROM gathering_member_map gmm
         JOIN gathering g ON g.gathering_code = gmm.gathering_code
WHERE gmm.member_id = p_member_id;

SET v_total_pages = IF(v_total = 0, 0, CEIL(v_total / p_page_size));

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
ORDER BY g.reg_time DESC
    LIMIT p_page_size OFFSET v_offset;
END//

DELIMITER ;
