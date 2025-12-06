package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.gathering.command.EditGatheringRequest;
import com.booktalk_be.domain.gathering.model.entity.*;
import com.booktalk_be.domain.gathering.model.repository.*;
import com.booktalk_be.domain.gathering.responseDto.GatheringDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringEditInitResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringResponse;
import com.booktalk_be.domain.hashtag.model.entity.HashTag;
import com.booktalk_be.domain.hashtag.model.entity.HashTagMap;
import com.booktalk_be.domain.hashtag.service.HashTagService;
import com.booktalk_be.domain.member.model.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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

    @Value("${app.upload.image-dir}")
    private String imageUploadDir;

    @Value("${app.upload.url-prefix:/uploads/images}")
    private String imageUrlPrefix;

    private final GatheringRepository gatheringRepository;
    private final GatheringMemberMapRepository gatheringMemberMapRepository;
    private final GatheringMemberMapService gatheringMemberMapService;
    private final GatheringBookMapService gatheringBookMapService;
    private final GatheringRecruitQuestionService  gatheringRecruitQuestionService;
    private final HashTagService hashTagService;

    //모임 리스트 전체조회 비즈니스 로직
    @Override
    public Page<GatheringResponse> getList(GatheringStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return gatheringRepository.findGatheringList(status, search, pageable);
    }

    // 모임개설 비즈니스 로직
    @Transactional
    @Override
    public void create(CreateGatheringCommand command, MultipartFile imageFile, Integer memberId) {
        // 0) 필수 검증
        if (memberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.(memberId null)");
        }
        if (command.getBooks() == null || command.getBooks().isEmpty()) {
            throw new IllegalArgumentException("도서 목록이 필요합니다.");
        }
        if (command.getQuestions() == null || command.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("참여 질문이 필요합니다.");
        }

        // 1) 이미지 저장 (있을 때만)
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = storeImage(imageFile);   // ★ 하드코딩 제거, 공용 메서드 사용
        }

        // 2) Gathering 생성
        Gathering gathering = Gathering.builder()
                .name(command.getGroupName())
                .recruitmentPersonnel(Long.parseLong(command.getRecruitmentPersonnel()))
                .recruitmentPeriod(command.getRecruitmentPeriod())
                .activityPeriod(command.getActivityPeriod())
                .summary(command.getMeetingDetails())
                .emdCd(command.getLocation() + "_읍면동 코드") // 임시
                .sigCd("행정구역코드")                         // 임시
                .imageData(imageUrl)
                .status(GatheringStatus.INTENDED)
                .build();

        Gathering gatheringSaved = gatheringRepository.save(gathering);

        // 3) 연관 데이터 저장
        gatheringMemberMapService.createGatheringMemberMap(gatheringSaved, memberId);
        gatheringBookMapService.createGatheringBookMap(gatheringSaved, command.getBooks());
        gatheringRecruitQuestionService.createRecruitQuestionMap(gatheringSaved, command.getQuestions());

        if (command.getHashtags() != null && !command.getHashtags().isEmpty()) {
            hashTagService.createHashTag(gatheringSaved, command.getHashtags());
        }
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

    @Transactional
    @Override
    public void softDeleteGathering(String code, String reason, Member member) {
        // 권한 체크: 방장만 삭제 가능
        int memberId = Objects.requireNonNull(member, "로그인이 필요합니다.").getMemberId();

        var gathering = gatheringRepository.findByCodeAndDelYnFalse(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이미 삭제되었거나 존재하지 않는 모임입니다."));

        boolean isMaster = gatheringMemberMapRepository
                .findMasterYn(code, memberId)
                .map(Boolean::booleanValue)
                .orElse(false);
        if (!isMaster) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        int updated = gatheringRepository.softDelete(code, reason);
        if (updated == 0) {
            // 동시성 등으로 이미 삭제되었을 수 있음
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 삭제된 모임입니다.");
        }
    }

    @Transactional
    @Override
    public void updateGathering(String code, EditGatheringRequest command, MultipartFile image, Member member) {
        // 1) 대상 모임 조회
        Gathering gathering = gatheringRepository.findByCodeAndDelYnFalse(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모임을 찾을 수 없습니다."));

        // 2) 권한 체크: 방장인지 확인
        int memberId = Objects.requireNonNull(member, "로그인이 필요합니다.").getMemberId();
        boolean isMaster = gatheringMemberMapRepository
                .findMasterYn(code, memberId)
                .map(Boolean::booleanValue)
                .orElse(false);

        if (!isMaster) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "모임 수정 권한이 없습니다.");
        }

        // 3) 상태 파싱 (옵션)
        GatheringStatus status = null;
        if (StringUtils.hasText(command.getStatus())) {
            try {
                status = GatheringStatus.valueOf(command.getStatus());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 모임 상태 값입니다.");
            }
        }

        // 4) 모집 인원 파싱 (옵션)
        Long recruitmentPersonnel = null;
        if (StringUtils.hasText(command.getRecruitmentPersonnel())) {
            try {
                recruitmentPersonnel = Long.parseLong(command.getRecruitmentPersonnel());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "모집 인원 형식이 올바르지 않습니다.");
            }
        }

        // 5) 엔티티에 값 반영 (null인 값은 무시)
        gathering.updateCore(
                command.getGroupName(),          // name
                recruitmentPersonnel,            // recruitmentPersonnel
                command.getRecruitmentPeriod(),  // recruitmentPeriod
                command.getActivityPeriod(),     // activityPeriod
                command.getLocation(),           // location → sigCd / emdCd
                command.getMeetingDetails(),     // summary
                status                           // status
        );

        // 6) 이미지 파일 교체 (있을 때만)
        if (image != null && !image.isEmpty()) {
            String imageUrl = storeImage(image);
            gathering.changeImage(imageUrl);
        }
        // 새 이미지 안 넘어오면 기존 imageUrl 유지
        // (command.getImageUrl()는 지금 단계에서는 굳이 안 건들어도 됨)

        // 다음 단계에서:
         gatheringBookMapService.syncBooks(gathering, command.getBooks());
         gatheringRecruitQuestionService.syncRecruitQuestions(gathering, command.getQuestions());
         hashTagService.syncHashtags(gathering, command.getHashtags());
        // 이런 식으로 연관관계 업데이트를 붙이면 된다.
    }

    private String storeImage(MultipartFile imageFile) {
        try {
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

            Path uploadPath = Paths.get(imageUploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return imageUrlPrefix + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }
}
