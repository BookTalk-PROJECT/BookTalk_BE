package com.booktalk_be.domain.gathering.controller;

import com.booktalk_be.common.utils.ResponseDto;
import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gathering")
@RequiredArgsConstructor
public class GatheringController {

    //private final GatheringService gatheringService;

    @PostMapping("/create")
    @Tag(name = "GatheringBoard API")
    @Operation(summary = "모임 개설", description = "모임을 개설합니다.")
    public ResponseEntity<ResponseDto> create(@RequestBody @Valid CreateGatheringCommand requestData) {
        //gatheringService.create(requestData, member);

        return ResponseEntity.ok(ResponseDto.builder()
                .code(200)
                .build());
    }
}
