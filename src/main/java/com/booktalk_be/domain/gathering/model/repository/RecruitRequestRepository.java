package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.RecruitRequest;
import com.booktalk_be.domain.gathering.model.entity.RecruitRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecruitRequestRepository extends JpaRepository<RecruitRequest, RecruitRequestId> {
    @Query(value = "CALL sp_mypage_recruit_request_list(:memberId, :pageNum, :pageSize)", nativeQuery = true)
    List<Object[]> callMyRecruitRequestList(
            @Param("memberId") int memberId,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );


    @Query(value = "CALL sp_mypage_recruit_approval_list(:masterId, :pageNum, :pageSize)", nativeQuery = true)
    List<Object[]> callRecruitApprovalList(
            @Param("masterId") int masterId,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );

    @Modifying
    @Query(value = "CALL sp_gathering_recruit_approve(:masterId, :gatheringCode, :applicantId)", nativeQuery = true)
    int callRecruitApprove(
            @Param("masterId") int masterId,
            @Param("gatheringCode") String gatheringCode,
            @Param("applicantId") int applicantId
    );

    @Modifying
    @Query(value = "CALL sp_gathering_recruit_reject(:masterId, :gatheringCode, :applicantId, :rejectReason)", nativeQuery = true)
    int callRecruitReject(
            @Param("masterId") int masterId,
            @Param("gatheringCode") String gatheringCode,
            @Param("applicantId") int applicantId,
            @Param("rejectReason") String rejectReason
    );
}
