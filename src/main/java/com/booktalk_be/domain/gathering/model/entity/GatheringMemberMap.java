package com.booktalk_be.domain.gathering.model.entity;

import com.booktalk_be.domain.member.model.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(GatheringMemberId.class)
@Table(name = "gathering_member_map") //모임_멤버 매핑 엔티티
public class GatheringMemberMap {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_code")
    private Gathering code;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "master_yn")
    private Boolean masterYn;
}
