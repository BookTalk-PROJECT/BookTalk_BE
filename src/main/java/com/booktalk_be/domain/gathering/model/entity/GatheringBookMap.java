package com.booktalk_be.domain.gathering.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(GatheringBookId.class)
@Table(name = "gathering_book_map")
public class GatheringBookMap {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_code")
    private Gathering code;

    @Id
    @Column(name = "isbn", nullable = false)
    private String isbn;

    @Column(name = "book_name", nullable = false)
    private String name;

    @Column(name = "orders", nullable = false)
    private Integer order;

    @Column(name = "complete_yn", nullable = false)
    private Boolean completeYn;
}
