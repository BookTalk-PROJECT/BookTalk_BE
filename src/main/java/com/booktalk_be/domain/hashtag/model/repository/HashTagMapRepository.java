package com.booktalk_be.domain.hashtag.model.repository;


import com.booktalk_be.domain.hashtag.model.entity.HashTagId;
import com.booktalk_be.domain.hashtag.model.entity.HashTagMap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashTagMapRepository extends JpaRepository<HashTagMap, HashTagId> {
}
