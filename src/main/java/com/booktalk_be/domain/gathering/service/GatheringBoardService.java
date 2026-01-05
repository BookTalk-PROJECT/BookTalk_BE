package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.common.responseDto.PageResponseDto;
import com.booktalk_be.domain.gathering.command.CreateGatheringBoardCommand;
import com.booktalk_be.domain.gathering.command.UpdateGatheringBoardCommand;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardResponse;
import com.booktalk_be.domain.member.model.entity.Member;

public interface GatheringBoardService {
    void create(CreateGatheringBoardCommand cmd, Member member);
    void modify(UpdateGatheringBoardCommand cmd);
    void delete(String postCode);
    PageResponseDto<GatheringBoardResponse> list(String gatheringCode, Integer pageNum, Integer pageSize);
    GatheringBoardDetailResponse detail(String postCode);
}