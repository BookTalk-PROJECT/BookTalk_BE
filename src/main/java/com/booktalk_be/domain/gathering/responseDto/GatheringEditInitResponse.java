package com.booktalk_be.domain.gathering.responseDto;


import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatheringEditInitResponse {

    // 기존 상세 DTO를 평탄화해서 그대로 노출
    @JsonUnwrapped
    private GatheringDetailResponse base;

    // 대표 이미지 URL 추가
    private String imageUrl;

    // 편집에 필요한 추가 데이터
    private List<BookItem> books;
    private List<QuestionItem> questions;
    private List<String> hashtags;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BookItem {
        private String isbn;
        private String name;
        private Long order;         // UI 정렬용(Long 통일)
        private String complete_yn; // "0" | "1" (프론트 생성 포맷과 호환)
        private String startDate;   // YYYY-MM-DD
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuestionItem {
        private Long id;
        private Integer order;      // 정렬용
        private String question;
    }
}