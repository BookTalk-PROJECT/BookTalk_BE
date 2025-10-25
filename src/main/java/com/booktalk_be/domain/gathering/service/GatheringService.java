package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import com.booktalk_be.domain.gathering.responseDto.GatheringDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface GatheringService {
    void create(CreateGatheringCommand command, MultipartFile imageFile, Integer memberId);

    Page<GatheringResponse> getList(GatheringStatus status, String search, int page, int size);

    GatheringDetailResponse getDetailByCode(String code, String currentMemberId);
}
