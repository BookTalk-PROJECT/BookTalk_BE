package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.GatheringRecruitQuestionId;
import com.booktalk_be.domain.gathering.model.entity.GatheringRecruitQuestionMap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRecruitQuestionMapRepository extends JpaRepository<GatheringRecruitQuestionMap, GatheringRecruitQuestionId> {
}
