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
import java.util.List;

@Service
@RequiredArgsConstructor
public class GatheringServiceImpl implements GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringBookMapRepository gatheringBookMapRepository;
    private final RecruitQuestionRepository recruitQuestionRepository;
    private final GatheringRecruitQuestionMapRepository gatheringRecruitQuestionMapRepository;

    // 모임개설 비즈니스 로직
    @Transactional
    @Override
    public void create(CreateGatheringCommand command, MultipartFile imageFile) {
        // 모집 정보 문자열 조합
        String recruitInfo = String.join("_",
                command.getLocation(),
                command.getRecruitmentPersonnel(),
                command.getRecruitmentPeriod(),
                command.getActivityPeriod()
        );

        byte[] imageBytes = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageBytes = imageFile.getBytes();
            } catch (IOException e) {
                throw new RuntimeException("이미지 파일 처리 실패", e);
            }
        }

        // Gathering 데이터 저장
        Gathering gathering = Gathering.builder()
                .name(command.getGroupName())
                .recruitInfo(recruitInfo)
                .summary(command.getMeetingDetails())
                .emdCd("팔용동") // 임시
                .sigCd("창원시") // 임시
                .imageData(imageBytes)
                .status(GatheringStatus.INTENDED)
                .build();

        Gathering gatheringSaved = gatheringRepository.save(gathering);

        // Book 데이터 저장
        if (command.getBooks() != null) {
            List<GatheringBookMap> bookMaps = command.getBooks().stream()
                    .map(book -> GatheringBookMap.builder()
                            .code(gatheringSaved)
                            .isbn(book.getIsbn())
                            .name(book.getName())
                            .order((int) book.getOrder())
                            .completeYn("complete".equals(book.getComplete_yn()))
                            .build())
                    .toList();
            gatheringBookMapRepository.saveAll(bookMaps);
        }
        System.out.println("여까진 오나?");
        //참여신청 질문 저장
        if (command.getQuestions() != null) {
            JsonPrinter.print(command.getQuestions());
            List<RecruitQuestion> questions = command.getQuestions().stream()
                    .map(question -> RecruitQuestion.builder()
                            .order(question.getId())
                            .question(question.getQuestion())
                            .build())
                    .toList();
            List<RecruitQuestion> questionsSaved = recruitQuestionRepository.saveAll(questions);

            // 2. 매핑 테이블에 저장
            List<GatheringRecruitQuestionMap> questionMaps = questionsSaved.stream()
                    .map(q -> GatheringRecruitQuestionMap.builder()
                            .code(gatheringSaved)               // FK: Gathering
                            .recruitQuestion(q)                 // FK: RecruitQuestion
                            .build())
                    .toList();
            gatheringRecruitQuestionMapRepository.saveAll(questionMaps);
        }
    }

    //모임 리스트 전체조회 비즈니스 로직
    @Override
    public Page<GatheringResponse> getList(GatheringStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return gatheringRepository.findGatheringList(status, search, pageable);
    }
}
