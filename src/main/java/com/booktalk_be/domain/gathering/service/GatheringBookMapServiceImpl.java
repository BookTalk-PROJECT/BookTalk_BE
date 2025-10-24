package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.BookDto;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringBookMap;
import com.booktalk_be.domain.gathering.model.repository.GatheringBookMapRepository;
import com.booktalk_be.domain.gathering.responseDto.BookItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
