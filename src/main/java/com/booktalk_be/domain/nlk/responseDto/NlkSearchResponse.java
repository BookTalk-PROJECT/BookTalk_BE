package com.booktalk_be.domain.nlk.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class NlkSearchResponse {
    private int total;
    private int pageNum;
    private int pageSize;
    private List<NlkBookDto> items;
}
