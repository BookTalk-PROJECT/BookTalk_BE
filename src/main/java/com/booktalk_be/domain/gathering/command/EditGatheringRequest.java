package com.booktalk_be.domain.gathering.command;

import com.booktalk_be.domain.gathering.command.CreateGatheringCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class EditGatheringRequest extends CreateGatheringCommand {
    // INTENDED | PROGRESS | END (없으면 상태 유지)
    private String status;

    // 현재 대표 이미지 URL(프론트 미리보기용). 새 파일 업로드 없으면 그대로 유지용 전달.
    private String imageUrl;
}