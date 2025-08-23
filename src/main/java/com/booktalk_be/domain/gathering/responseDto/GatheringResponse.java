package com.booktalk_be.domain.gathering.responseDto;

import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
@Builder
//모임 리스트 조회 응답 데이터
public class GatheringResponse {
    private String code;
    private String title;
    private int views;               // 임시 랜덤값
    private int currentMembers;      // 임시 랜덤값
    private Long maxMembers;
    private GatheringStatus status;
    private String imageUrl;
    private List<String> hashtags;   // 임시 더미 데이터
}
