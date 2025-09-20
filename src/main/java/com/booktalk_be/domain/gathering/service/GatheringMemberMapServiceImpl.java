package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringBookMap;
import com.booktalk_be.domain.gathering.model.entity.GatheringMemberMap;
import com.booktalk_be.domain.gathering.model.repository.GatheringMemberMapRepository;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatheringMemberMapServiceImpl implements GatheringMemberMapService {

    private final MemberRepository memberRepository;
    private final GatheringMemberMapRepository gatheringMemberMapRepository;

    public void createGatheringMemberMap(Gathering gatheringSaved, String memberId){
        if (memberId != null) {

            Member memberRef = memberRepository.getReferenceById(Integer.parseInt(memberId));
            GatheringMemberMap gatheringMemberMap = GatheringMemberMap.builder()
                    .code(gatheringSaved)
                    .member(memberRef)
                    .masterYn(true)
                    .build();
            gatheringMemberMapRepository.save(gatheringMemberMap);
        }
    };
}
