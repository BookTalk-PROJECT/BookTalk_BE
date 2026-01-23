DELIMITER //

DROP PROCEDURE IF EXISTS sp_mypage_recruit_request_list //

CREATE PROCEDURE sp_mypage_recruit_request_list(
    IN p_member_id INT,
    IN p_page_num INT,
    IN p_page_size INT
)
BEGIN
    DECLARE v_offset INT DEFAULT 0;
    DECLARE v_total BIGINT DEFAULT 0;
    DECLARE v_total_pages INT DEFAULT 0;

    -- GROUP_CONCAT 길이 늘려야 답변 길 때 안 잘림
SET SESSION group_concat_max_len = 1000000;

SET v_offset = (p_page_num - 1) * p_page_size;

    -- 신청 "건" 기준 (질문 row 기준으로 세면 망함)
SELECT COUNT(DISTINCT rr.gathering_code)
INTO v_total
FROM recruit_request rr
WHERE rr.member_id = p_member_id;

SET v_total_pages = IF(v_total = 0, 0, CEIL(v_total / p_page_size));

SELECT
    t.gathering_code AS gathering_code,
    t.gathering_name AS gathering_name,
    t.qa_json AS qa_json,
    t.status AS status,
    t.reject_reason AS reject_reason,
    v_total_pages AS total_pages
FROM (
         SELECT
             rr.gathering_code,
             g.name AS gathering_name,

             -- 질문 순서 보장 JSON 배열 문자열 생성
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
                  JOIN gathering g
                       ON g.gathering_code = rr.gathering_code
                  LEFT JOIN recruit_question rq
                            ON rq.recruit_question = CAST(rr.recruit_question AS UNSIGNED)
         WHERE rr.member_id = p_member_id
         GROUP BY rr.gathering_code, g.name
         ORDER BY rr.gathering_code DESC
             LIMIT p_page_size OFFSET v_offset
     ) t;
END//

DELIMITER ;
