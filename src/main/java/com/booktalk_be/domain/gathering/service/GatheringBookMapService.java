package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.BookDto;
import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringBookMap;
import com.booktalk_be.domain.gathering.responseDto.BookItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface GatheringBookMapService {
    void createGatheringBookMap(Gathering gatheringSaved, List<BookDto> books);

    List<BookItemResponse> getBooksByGatheringCode(String code);

    List<GatheringBookMap> findAllByGathering(Gathering gathering);

    void syncBooks(Gathering gathering, List<BookDto> books);
}
