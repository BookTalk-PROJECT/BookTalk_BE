package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import org.springframework.stereotype.Service;

@Service
public interface GatheringMemberMapService {
    void createGatheringMemberMap(Gathering gatheringSaved, String memberId);
}
