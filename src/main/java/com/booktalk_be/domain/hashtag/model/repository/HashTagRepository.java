package com.booktalk_be.domain.hashtag.model.repository;

import com.booktalk_be.domain.hashtag.model.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashTagRepository extends JpaRepository<HashTag, Long>,HashTagRepositoryCustom {
}
