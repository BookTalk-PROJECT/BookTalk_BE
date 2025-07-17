package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import com.booktalk_be.domain.gathering.responseDto.GatheringResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GatheringRepositoryCustom{
    Page<GatheringResponse> findGatheringList(GatheringStatus status, String search, Pageable pageable);
}
