package com.booktalk_be.domain.gathering.service;


import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.gathering.command.RecruitRequestCommand;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageRecruitApprovalResponse;
import com.booktalk_be.domain.gathering.responseDto.mypage.MyPageRecruitRequestResponse;
import com.booktalk_be.domain.member.model.entity.Member;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GatheringRecruitRequestService {
    void submit(String gatheringCode, Member memberId, RecruitRequestCommand command);

    PageResponseDto<MyPageRecruitRequestResponse> getMyRecruitRequests(Integer pageNum, Integer pageSize, int memberId);

    PageResponseDto<MyPageRecruitApprovalResponse> getApprovalList(Integer pageNum, Integer pageSize, int masterId);

    void approve(int masterId, String gatheringCode, int applicantId);

    void reject(int masterId, String gatheringCode, int applicantId, String rejectReason);
}
