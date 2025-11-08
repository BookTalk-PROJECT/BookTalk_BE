package com.booktalk_be.common.command;

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
public class ReplySearchCondCommand extends SearchCondCommand {

    @NotNull
    @JsonProperty("keywordType")
    private CommentKeywordType type;

    @RequiredArgsConstructor
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum CommentKeywordType implements EntityEnumerable {
        POST_CODE("post_code", "게시글 코드"),
        REPLY_CODE("reply_code", "댓글 번호"),
        CONTENT("content", "댓글 내용"),
        AUTHOR("author", "작성자");

        private final String type;
        @Getter
        private final String name;

        @JsonValue
        public String getType() {
            return type;
        }

        //Request Body로 부터 수신한 type string value를 매칭된 ENUM 타입으로 매핑
        @JsonCreator
        public static CommentKeywordType fromType(String value) {
            for (CommentKeywordType ct : values()) {
                if (ct.type.equalsIgnoreCase(value)) {
                    return ct;
                }
            }
            throw new IllegalArgumentException("Unknown KeywordType: " + value);
        }

    }

}
