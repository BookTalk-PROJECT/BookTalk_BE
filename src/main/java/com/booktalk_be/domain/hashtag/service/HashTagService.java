package com.booktalk_be.domain.hashtag.service;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.hashtag.model.entity.HashTag;
import com.booktalk_be.domain.hashtag.model.entity.HashTagMap;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HashTagService {
    void createHashTag(Gathering gathering, List<String> hashTags);

    List<HashTagMap> findAllByGathering(Gathering gathering);

    void syncHashtags(Gathering gathering, List<String> tags);
}
