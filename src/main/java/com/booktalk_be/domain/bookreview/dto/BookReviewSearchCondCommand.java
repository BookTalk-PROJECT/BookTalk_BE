package com.booktalk_be.domain.bookreview.dto;

import com.booktalk_be.common.command.PostSearchCondCommand;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookReviewSearchCondCommand extends PostSearchCondCommand {
    // This class extends PostSearchCondCommand, which already has keywordType, keyword, startDate, endDate.
    // No additional fields are needed for now, but it's a dedicated class for BookReview search.
}
