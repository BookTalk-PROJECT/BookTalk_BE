package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.GatheringMemberId;
import com.booktalk_be.domain.gathering.model.entity.GatheringMemberMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatheringMemberMapRepository extends JpaRepository<GatheringMemberMap, GatheringMemberId> {
}
