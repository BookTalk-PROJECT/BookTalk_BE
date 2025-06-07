package com.booktalk_be.domain.gathering.responseDto;

import com.booktalk_be.domain.gathering.command.BookDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
@Builder
public class GatheringResponse {
    private String groupName;
    private String location;
    private String meetingDetails;
    private String recruitmentPersonnel;
    private String recruitmentPeriod;
    private String activityPeriod;

    private List<BookDto> books;
    private List<String> questions;
    private List<String> hashtags;
}
