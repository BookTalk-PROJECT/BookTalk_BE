package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.RecruitRequest;
import com.booktalk_be.domain.gathering.model.entity.RecruitRequestId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitRequestRepository extends JpaRepository<RecruitRequest, RecruitRequestId> {
}
