package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.common.utils.JsonPrinter;
import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.gathering.model.entity.*;
import com.booktalk_be.domain.gathering.model.repository.*;
import com.booktalk_be.domain.gathering.responseDto.GatheringResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GatheringServiceImpl implements GatheringService {

    private final GatheringRepository gatheringRepository;
    private final RecruitQuestionRepository recruitQuestionRepository;
    private final GatheringBookMapService gatheringBookMapService;
    private final GatheringRecruitQuestionService  gatheringRecruitQuestionService;

    // 모임개설 비즈니스 로직
    @Transactional
    @Override
    public void create(CreateGatheringCommand command, MultipartFile imageFile) {
        // 모집 정보 문자열 조합

        String imageUrl = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 파일 저장 경로 지정
                String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/images");

                // 디렉토리가 없으면 생성
                Files.createDirectories(uploadPath);

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                imageUrl = "/uploads/images/" + fileName; // 또는 외부 URL
            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }


        // Gathering 데이터 저장
        Gathering gathering = Gathering.builder()
                .name(command.getGroupName())
                .recruitmentPersonnel(Long.parseLong(command.getRecruitmentPersonnel()))
                .recruitmentPeriod(command.getRecruitmentPeriod())
                .activityPeriod(command.getActivityPeriod())
                .summary(command.getMeetingDetails())
                .emdCd(command.getLocation()+"_읍면동 코드") // 임시로 Location과 임시 문자열 추가
                .sigCd("행정구역코드") // 임시
                .imageData(imageUrl)
                .status(GatheringStatus.INTENDED)
                .build();

        Gathering gatheringSaved = gatheringRepository.save(gathering);

        //책 리스트 매핑 테이블에 저장
        if(command.getBooks() != null){
            gatheringBookMapService.createGatheringBookMap(gatheringSaved, command.getBooks());
        }
        //참여신청 질문 리스트 저장
        if(command.getQuestions() != null){
            gatheringRecruitQuestionService.createRecruitQuestionMap(gatheringSaved, command.getQuestions());
        }
    }

    //모임 리스트 전체조회 비즈니스 로직
    @Override
    public Page<GatheringResponse> getList(GatheringStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return gatheringRepository.findGatheringList(status, search, pageable);
    }
}
