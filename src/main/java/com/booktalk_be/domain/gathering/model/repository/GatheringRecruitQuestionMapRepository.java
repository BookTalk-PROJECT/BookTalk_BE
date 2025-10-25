package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.GatheringRecruitQuestionId;
import com.booktalk_be.domain.gathering.model.entity.GatheringRecruitQuestionMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GatheringRecruitQuestionMapRepository extends JpaRepository<GatheringRecruitQuestionMap, GatheringRecruitQuestionId> {

    // 모임 코드로 연결된 질문 리스트, 질문의 order 기준 정렬
    @Query("""
        select gm
        from GatheringRecruitQuestionMap gm
        join fetch gm.recruitQuestion rq
        where gm.code.code = :code
        order by rq.order asc
    """)
    List<GatheringRecruitQuestionMap> findAllByGatheringCodeOrderByQuestionOrder(@Param("code") String gatheringCode);

}
