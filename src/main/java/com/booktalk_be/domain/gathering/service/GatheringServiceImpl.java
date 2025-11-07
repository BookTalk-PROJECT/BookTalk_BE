package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.gathering.model.entity.*;
import com.booktalk_be.domain.gathering.model.repository.*;
import com.booktalk_be.domain.gathering.responseDto.GatheringDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringEditInitResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringResponse;
import com.booktalk_be.domain.hashtag.model.entity.HashTag;
import com.booktalk_be.domain.hashtag.model.entity.HashTagMap;
import com.booktalk_be.domain.hashtag.service.HashTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GatheringServiceImpl implements GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringMemberMapRepository gatheringMemberMapRepository;
    private final GatheringMemberMapService gatheringMemberMapService;
    private final GatheringBookMapService gatheringBookMapService;
    private final GatheringRecruitQuestionService  gatheringRecruitQuestionService;
    private final HashTagService hashTagService;

    // 모임개설 비즈니스 로직
    @Transactional
    @Override
    public void create(CreateGatheringCommand command, MultipartFile imageFile, Integer memberId) {
        // 모집 정보 문자열 조합

        // 0) 필수 검증: 하나라도 없으면 예외 → 트랜잭션 전체 롤백
        if (memberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.(memberId null)");
        }
        if (command.getBooks() == null || command.getBooks().isEmpty()) {
            throw new IllegalArgumentException("도서 목록이 필요합니다.");
        }
        if (command.getQuestions() == null || command.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("참여 질문이 필요합니다.");
        }

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

        //유저 모임 매핑 테이블에 저장
        gatheringMemberMapService.createGatheringMemberMap(gatheringSaved, memberId);
        //책 리스트 매핑 테이블에 저장
        gatheringBookMapService.createGatheringBookMap(gatheringSaved, command.getBooks());
        //참여신청 질문 리스트 저장
        gatheringRecruitQuestionService.createRecruitQuestionMap(gatheringSaved, command.getQuestions());

        if(command.getHashtags() != null && !command.getHashtags().isEmpty()) {
            hashTagService.createHashTag(gatheringSaved, command.getHashtags());
        }
    }

    //모임 리스트 전체조회 비즈니스 로직
    @Override
    public Page<GatheringResponse> getList(GatheringStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return gatheringRepository.findGatheringList(status, search, pageable);
    }

    @Override
    public GatheringDetailResponse getDetailByCode(String code, int currentMemberId) {
        var g = gatheringRepository.findByCodeAndDelYnFalse(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."));

        int masterYn = 0;
        masterYn = gatheringMemberMapRepository
                .findMasterYn(code, currentMemberId)
                .map(b -> Boolean.TRUE.equals(b) ? 1 : 0)
                .orElse(0);

        return GatheringDetailResponse.from(g, masterYn);
    }

    // ====== 편집 초기값(상세 + 책/질문/태그) ======
    @Transactional(readOnly = true)
    @Override
    public GatheringEditInitResponse getEditInitByCode(String code, int currentMemberId) {
        Gathering gathering = gatheringRepository.findByCodeAndDelYnFalse(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."));

        GatheringDetailResponse base = getDetailByCode(code, currentMemberId);

        var bookRows = gatheringBookMapService.findAllByGathering(gathering);
        var books = bookRows.stream()
                .sorted(Comparator.comparing(GatheringBookMap::getOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(b -> GatheringEditInitResponse.BookItem.builder()
                        .isbn(b.getIsbn())
                        .name(b.getName())
                        .order(b.getOrder() == null ? null : b.getOrder().longValue())
                        .complete_yn(Boolean.TRUE.equals(b.getCompleteYn()) ? "1" : "0")
                        .startDate(b.getStartDate())
                        .build())
                .toList();

        var qMaps = gatheringRecruitQuestionService.findAllByGathering(gathering);
        var questions = qMaps.stream()
                .map(GatheringRecruitQuestionMap::getRecruitQuestion)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(RecruitQuestion::getOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(q -> GatheringEditInitResponse.QuestionItem.builder()
                        .id(q.getRecruit_question())
                        .order(q.getOrder())
                        .question(q.getQuestion())
                        .build())
                .toList();

        var hashtags = hashTagService.findAllByGathering(gathering).stream()
                .map(HashTagMap::getHashtagId)
                .filter(Objects::nonNull)
                .map(HashTag::getValue)
                .toList();

        return GatheringEditInitResponse.builder()
                .base(base)
                .imageUrl(gathering.getImageUrl())   // ★ 여기
                .books(books)
                .questions(questions)
                .hashtags(hashtags)
                .build();
    }

}
