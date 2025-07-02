package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringBookMap;
import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import com.booktalk_be.domain.gathering.model.repository.GatheringBookMapRepository;
import com.booktalk_be.domain.gathering.model.repository.GatheringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatheringServiceImpl implements GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringBookMapRepository gatheringBookMapRepository;

    @Transactional
    @Override
    public void create(CreateGatheringCommand command) {
        // 모집 정보 문자열 조합
        String recruitInfo = String.join("_",
                command.getLocation(),
                command.getRecruitmentPersonnel(),
                command.getRecruitmentPeriod(),
                command.getActivityPeriod()
        );

        Gathering gathering = Gathering.builder()
                .name(command.getGroupName())
                .recruitInfo(recruitInfo)
                .summary(command.getMeetingDetails())
                .emdCd("팔용동") // 임시
                .sigCd("창원시") // 임시
                .status(GatheringStatus.INTENDED)
                .build();

        Gathering saved = gatheringRepository.save(gathering);

        // Book 데이터 저장
        if (command.getBooks() != null) {
            List<GatheringBookMap> bookMaps = command.getBooks().stream()
                    .map(book -> GatheringBookMap.builder()
                            .code(saved)
                            .isbn(book.getIsbn())
                            .name(book.getName())
                            .order((int) book.getOrder())
                            .completeYn("complete".equals(book.getComplete_yn()))
                            .build())
                    .toList();
            gatheringBookMapRepository.saveAll(bookMaps);
        }
    }

}
