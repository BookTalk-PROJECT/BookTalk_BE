package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.BookDto;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringBookMap;
import com.booktalk_be.domain.gathering.model.repository.GatheringBookMapRepository;
import com.booktalk_be.domain.gathering.responseDto.BookItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatheringBookMapServiceImpl implements GatheringBookMapService {

    private final GatheringBookMapRepository gatheringBookMapRepository;

    public void createGatheringBookMap(Gathering gatheringSaved, List<BookDto> books) {
        // Book 데이터 저장
        if (books != null) {
            List<GatheringBookMap> bookMaps = books.stream()
                    .map(book -> GatheringBookMap.builder()
                            .code(gatheringSaved)
                            .isbn(book.getIsbn())
                            .name(book.getName())
                            .order((int) book.getOrder())
                            .completeYn("complete".equals(book.getComplete_yn()))
                            .startDate(book.getStartDate())
                            .build())
                    .toList();
            gatheringBookMapRepository.saveAll(bookMaps);
        }
    }


    @Override
    public List<BookItemResponse> getBooksByGatheringCode(String code) {
        return gatheringBookMapRepository.findAllByGatheringCode(code).stream()
                .map(BookItemResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GatheringBookMap> findAllByGathering(Gathering gathering) {
        return gatheringBookMapRepository.findAllByCode(gathering);
    }

    @Override
    @Transactional
    public void syncBooks(Gathering gathering, List<BookDto> books) {
        // 1) 기존 데이터 로드
        List<GatheringBookMap> existing = gatheringBookMapRepository.findAllByCode(gathering);
        Map<String, GatheringBookMap> existingByIsbn = existing.stream()
                .collect(Collectors.toMap(GatheringBookMap::getIsbn, b -> b));

        Set<String> seen = new HashSet<>();

        // 2) 들어온 DTO 기준으로 upsert
        if (books != null) {
            for (int i = 0; i < books.size(); i++) {
                BookDto dto = books.get(i);
                if (dto == null || !StringUtils.hasText(dto.getIsbn())) continue;

                String isbn = dto.getIsbn();
                seen.add(isbn);

                Integer order = safeOrder(dto.getOrder(), i);
                Boolean completeYn = parseComplete(dto.getComplete_yn());

                GatheringBookMap entity = existingByIsbn.get(isbn);
                if (entity == null) {
                    // 신규
                    entity = GatheringBookMap.builder()
                            .code(gathering)
                            .isbn(isbn)
                            .name(dto.getName())
                            .order(order)
                            .completeYn(completeYn != null ? completeYn : Boolean.FALSE)
                            .startDate(dto.getStartDate())
                            .end_date(null)
                            .build();
                    gatheringBookMapRepository.save(entity);
                } else {
                    // 기존 → 값 갱신 (end_date는 그대로 유지)
                    entity.updateBook(
                            dto.getName(),
                            order,
                            completeYn,
                            dto.getStartDate(),
                            null
                    );
                    // save 필요 없음. 영속 상태라 더티체킹으로 반영됨.
                }
            }
        }

        // 3) DTO에 더이상 없는 ISBN들은 삭제
        List<GatheringBookMap> toDelete = existing.stream()
                .filter(b -> !seen.contains(b.getIsbn()))
                .toList();

        if (!toDelete.isEmpty()) {
            gatheringBookMapRepository.deleteAll(toDelete);
        }
    }

    private Integer safeOrder(long order, int fallback) {
        try {
            return Math.toIntExact(order);
        } catch (ArithmeticException e) {
            return fallback;
        }
    }

    private Boolean parseComplete(String v) {
        if (!StringUtils.hasText(v)) return null;
        String s = v.trim().toLowerCase();

        // 프론트에서 0/1 숫자로 보내든, 문자열로 보내든 대충 다 받아줌
        if (s.equals("1") || s.equals("y") || s.equals("true")) return Boolean.TRUE;
        if (s.equals("0") || s.equals("n") || s.equals("false")) return Boolean.FALSE;

        // 그 외 값은 null → 기존 값 유지
        return null;
    }
}
