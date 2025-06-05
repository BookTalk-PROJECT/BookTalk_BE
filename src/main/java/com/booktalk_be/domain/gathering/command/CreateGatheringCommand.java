package com.booktalk_be.domain.gathering.command;


import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateGatheringCommand{
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