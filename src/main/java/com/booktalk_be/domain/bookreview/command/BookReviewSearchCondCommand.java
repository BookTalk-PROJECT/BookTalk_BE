package com.booktalk_be.domain.bookreview.command;

import com.booktalk_be.common.command.SearchCondCommand;
import com.booktalk_be.common.utils.EntityEnumerable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookReviewSearchCondCommand extends SearchCondCommand {

    @NotNull
    @JsonProperty("keywordType")
    private KeywordType type;

    @RequiredArgsConstructor
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum KeywordType implements EntityEnumerable {
        TITLE("title", "제목"),
        AUTHOR("author", "저자"),
        BOOK_TITLE("book_title", "도서명"), // Added for book reviews
        ISBN("isbn", "ISBN"); // Added for book reviews

        private final String type;
        @Getter
        private final String name;

        @JsonValue
        public String getType() {
            return type;
        }

        //Request Body로 부터 수신한 type string value를 매칭된 ENUM 타입으로 매핑
        @JsonCreator
        public static BookReviewSearchCondCommand.KeywordType fromType(String value) {
            for (BookReviewSearchCondCommand.KeywordType kt : values()) {
                if (kt.type.equalsIgnoreCase(value)) {
                    return kt;
                }
            }
            throw new IllegalArgumentException("Unknown KeywordType: " + value);
        }

    }
}
