package com.booktalk_be.common.command;


import com.booktalk_be.common.utils.EntityEnumerable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@Builder
public class PostSearchCondCommand extends SearchCondCommand {

    @NotNull
    @JsonProperty("keywordType")
    private KeywordType type;

    @RequiredArgsConstructor
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum KeywordType implements EntityEnumerable {
        CODE("board_code", "코드"),
        TITLE("title", "제목"),
        AUTHOR("author", "작성자"),
        CATEGORY("category", "카테고리");

        private final String type;
        @Getter
        private final String name;

        @JsonValue
        public String getType() {
            return type;
        }

        //Request Body로 부터 수신한 type string value를 매칭된 ENUM 타입으로 매핑
        @JsonCreator
        public static KeywordType fromType(String value) {
            for (KeywordType kt : values()) {
                if (kt.type.equalsIgnoreCase(value)) {
                    return kt;
                }
            }
            throw new IllegalArgumentException("Unknown KeywordType: " + value);
        }

    }
}

