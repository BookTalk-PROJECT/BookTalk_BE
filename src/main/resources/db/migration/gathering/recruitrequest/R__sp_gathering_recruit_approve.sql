DELIMITER //

DROP PROCEDURE IF EXISTS sp_gathering_recruit_approve //

CREATE PROCEDURE sp_gathering_recruit_approve(
    IN p_master_id INT,
    IN p_gathering_code VARCHAR(50),
    IN p_applicant_id INT
)
BEGIN
    -- 모임장 권한 체크
    IF NOT EXISTS (
        SELECT 1
          FROM gathering_member_map
         WHERE gathering_code = p_gathering_code
           AND member_id = p_master_id
           AND master_yn = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_MASTER';
END IF;

    -- 멤버 매핑 추가(이미 있으면 유지)
INSERT INTO gathering_member_map (gathering_code, member_id, master_yn)
VALUES (p_gathering_code, p_applicant_id, 0)
    ON DUPLICATE KEY UPDATE master_yn = master_yn;

-- 신청 데이터 삭제(질문 row 전체)
DELETE FROM recruit_request
WHERE gathering_code = p_gathering_code
  AND member_id = p_applicant_id;
END//

DELIMITER ;
