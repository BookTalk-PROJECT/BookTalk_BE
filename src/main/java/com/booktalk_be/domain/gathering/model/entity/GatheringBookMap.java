package com.booktalk_be.domain.gathering.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column(name = "start_date", nullable = false)
    private String startDate;

    @Column(name = "end_date")
    private String end_date;

    public void updateBook(String name,
                           Integer order,
                           Boolean completeYn,
                           String startDate,
                           String endDate) {

        if (name != null) {
            this.name = name;
        }
        if (order != null) {
            this.order = order;
        }
        if (completeYn != null) {
            this.completeYn = completeYn;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.end_date = endDate;
        }
    }
}
