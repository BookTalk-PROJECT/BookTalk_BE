DELIMITER //

DROP PROCEDURE IF EXISTS sp_gathering_recruit_reject //

CREATE PROCEDURE sp_gathering_recruit_reject(
    IN p_master_id INT,
    IN p_gathering_code VARCHAR(50),
    IN p_applicant_id INT,
    IN p_reject_reason VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM gathering_member_map
         WHERE gathering_code = p_gathering_code
           AND member_id = p_master_id
           AND master_yn = 1
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'NOT_MASTER';
END IF;

UPDATE recruit_request
SET status = 'REJECT',
    reject_reason = p_reject_reason
WHERE gathering_code = p_gathering_code
  AND member_id = p_applicant_id;
END//

DELIMITER ;
