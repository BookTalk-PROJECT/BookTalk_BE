package com.booktalk_be.domain.hashtag.service;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.hashtag.model.entity.HashTag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HashTagService {
    void createHashTag(Gathering gathering, List<String> hashTags);
}
