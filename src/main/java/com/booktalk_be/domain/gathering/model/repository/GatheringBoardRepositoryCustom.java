package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.responseDto.GatheringBoardPostDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GatheringBoardRepositoryCustom {
    Page<GatheringBoardResponse> findBoardsForPaging(String gatheringCode, Pageable pageable);
    GatheringBoardPostDetailResponse getBoardDetailBy(String postCode);
}
