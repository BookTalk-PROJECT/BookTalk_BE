package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.GatheringMemberId;
import com.booktalk_be.domain.gathering.model.entity.GatheringMemberMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GatheringMemberMapRepository extends JpaRepository<GatheringMemberMap, GatheringMemberId> {
    @Query("""
        select gm.masterYn
        from GatheringMemberMap gm
        where gm.code.code = :gatheringCode
          and gm.member.memberId = :memberId
    """)
    Optional<Boolean> findMasterYn(String gatheringCode, int memberId);
}
