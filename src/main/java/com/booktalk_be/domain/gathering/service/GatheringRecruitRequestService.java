package com.booktalk_be.domain.gathering.service;


import com.booktalk_be.domain.gathering.command.RecruitRequestCommand;
import com.booktalk_be.domain.member.model.entity.Member;

public interface GatheringRecruitRequestService {
    void submit(String gatheringCode, Member memberId, RecruitRequestCommand command);
}
