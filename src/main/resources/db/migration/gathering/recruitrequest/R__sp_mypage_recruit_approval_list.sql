DELIMITER //

DROP PROCEDURE IF EXISTS sp_mypage_recruit_approval_list //

CREATE PROCEDURE sp_mypage_recruit_approval_list(
    IN p_master_id INT,
    IN p_page_num INT,
    IN p_page_size INT
)
BEGIN
    DECLARE v_offset INT DEFAULT 0;
    DECLARE v_total BIGINT DEFAULT 0;
    DECLARE v_total_pages INT DEFAULT 0;

SET SESSION group_concat_max_len = 1000000;
SET v_offset = (p_page_num - 1) * p_page_size;

    -- (모임 + 신청자) 단위로 COUNT 해야 함
SELECT COUNT(*)
INTO v_total
FROM (
         SELECT rr.gathering_code, rr.member_id
         FROM recruit_request rr
                  JOIN gathering_member_map gmm
                       ON gmm.gathering_code = rr.gathering_code
                           AND gmm.member_id = p_master_id
                           AND gmm.master_yn = 1
         GROUP BY rr.gathering_code, rr.member_id
     ) x;

SET v_total_pages = IF(v_total = 0, 0, CEIL(v_total / p_page_size));

SELECT
    t.gathering_code,
    t.gathering_name,
    t.applicant_id,
    t.applicant_name,
    t.qa_json,
    t.status,
    t.reject_reason,
    v_total_pages AS total_pages
FROM (
         SELECT
             rr.gathering_code,
             g.name AS gathering_name,
             rr.member_id AS applicant_id,
             m.name AS applicant_name,

             CONCAT(
                     '[',
                     IFNULL(
                             GROUP_CONCAT(
                                     JSON_OBJECT(
                                             'question', rq.question,
                                             'answer', rr.request_question_answer
                                     )
                                         ORDER BY rq.question_order
              SEPARATOR ','
                             ),
                             ''
                     ),
                     ']'
             ) AS qa_json,

             MAX(rr.status) AS status,
             MAX(rr.reject_reason) AS reject_reason

         FROM recruit_request rr
                  JOIN gathering_member_map gmm
                       ON gmm.gathering_code = rr.gathering_code
                           AND gmm.member_id = p_master_id
                           AND gmm.master_yn = 1

                  JOIN gathering g
                       ON g.gathering_code = rr.gathering_code

                  JOIN member m
                       ON m.member_id = rr.member_id

                  LEFT JOIN recruit_question rq
                            ON rq.recruit_question = CAST(rr.recruit_question AS UNSIGNED)

         GROUP BY rr.gathering_code, g.name, rr.member_id, m.name
         ORDER BY rr.gathering_code DESC, rr.member_id DESC
             LIMIT p_page_size OFFSET v_offset
     ) t;
END//

DELIMITER ;
