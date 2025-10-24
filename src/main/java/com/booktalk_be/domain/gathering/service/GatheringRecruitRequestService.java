package com.booktalk_be.domain.gathering.service;


import com.booktalk_be.domain.gathering.command.RecruitRequestCommand;

public interface GatheringRecruitRequestService {
    void submit(String gatheringCode, String memberId, RecruitRequestCommand command);
}
