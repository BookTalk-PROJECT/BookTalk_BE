package com.booktalk_be.domain.gathering.service;

import com.booktalk_be.domain.gathering.command.BookDto;
import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import com.booktalk_be.domain.gathering.model.entity.Gathering;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface GatheringBookMapService {
    void createGatheringBookMap(Gathering gatheringSaved, List<BookDto> books);
}
