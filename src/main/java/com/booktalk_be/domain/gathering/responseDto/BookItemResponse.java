package com.booktalk_be.domain.gathering.responseDto;

import com.booktalk_be.domain.gathering.model.entity.GatheringBookMap;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class BookItemResponse {
    private int id;            // orders 사용
    private String title;      // name
    private int status;        // completeYn -> 1/0
    private String startDate;  // start_date
    private String endDate;    // end_date (nullable)

    public static BookItemResponse from(GatheringBookMap m) {
        return BookItemResponse.builder()
                .id(m.getOrder() == null ? 0 : m.getOrder())
                .title(m.getName())
                .status(Boolean.TRUE.equals(m.getCompleteYn()) ? 1 : 0)
                .startDate(m.getStartDate())
                .endDate(m.getEnd_date())
                .build();
    }
}