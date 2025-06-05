package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.domain.member.auth.model.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@IdClass(GatheringMemberMap.class)
@Table(name = "gathering_member_map")
public class GatheringMemberMap {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Gathering gathering;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Member member;

    @Column(name = "master_yn")
    private Boolean masterYn;
}
