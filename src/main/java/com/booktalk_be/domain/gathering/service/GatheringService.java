package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.member.auth.model.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface GatheringService {
    void create(CreateGatheringCommand command, MultipartFile imageFile);
}
