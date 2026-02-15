package com.booktalk_be.common.command;

import com.booktalk_be.common.command.SearchCondCommand;
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
public class MemberSearchCondCommand extends SearchCondCommand {

    @NotNull
    @JsonProperty("keywordType")
    private KeywordType type;

    @RequiredArgsConstructor
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum KeywordType implements EntityEnumerable {
        NAME("name", "이름"),
        EMAIL("email", "이메일"),
        PHONE("phone", "전화번호");

        private final String type;

        @Getter
        private final String name;

        @Override // EntityEnumerable 인터페이스 구현
        @JsonValue
        public String getType() {
            return type;
        }

        // Request Body로부터 수신한 type string value를 매칭된 ENUM 타입으로 매핑
        @JsonCreator
        public static KeywordType fromType(String value) {
            for (KeywordType kt : values()) {
                if (kt.type.equalsIgnoreCase(value)) {
                    return kt;
                }
            }
            // 유효하지 않은 타입이 들어오면 예외 발생 혹은 null 처리 (여기선 예외)
            throw new IllegalArgumentException("Unknown Member KeywordType: " + value);
        }
    }
}