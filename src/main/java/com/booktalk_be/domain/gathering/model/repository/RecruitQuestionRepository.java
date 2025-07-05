package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.RecruitQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitQuestionRepository extends JpaRepository<RecruitQuestion, Integer> {
}
