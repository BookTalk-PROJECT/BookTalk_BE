package com.booktalk_be.domain.hashtag.model.repository;


import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.hashtag.model.entity.HashTagId;
import com.booktalk_be.domain.hashtag.model.entity.HashTagMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HashTagMapRepository extends JpaRepository<HashTagMap, HashTagId> {
    List<HashTagMap> findAllByCode(Gathering code);
}
