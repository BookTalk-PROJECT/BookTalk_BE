package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringBookId;
import com.booktalk_be.domain.gathering.model.entity.GatheringBookMap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringBookMapRepository extends JpaRepository<GatheringBookMap, GatheringBookId> {
}
