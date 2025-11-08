package com.booktalk_be.domain.hashtag.service;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.hashtag.model.entity.HashTag;
import com.booktalk_be.domain.hashtag.model.entity.HashTagMap;
import com.booktalk_be.domain.hashtag.model.repository.HashTagMapRepository;
import com.booktalk_be.domain.hashtag.model.repository.HashTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HashTagServiceImpl implements HashTagService {
    private final HashTagRepository hashTagRepository;
    private final HashTagMapRepository hashTagMapRepository;

    public void createHashTag(Gathering gathering, List<String> tags) {
        // 1. 해시태그 저장
        List<HashTag> hashTags = tags.stream()
                .map(tagChild -> HashTag.builder()
                        .value(tagChild)
                        .build()
        ).toList();

        List<HashTag> hashTagsSaved = hashTagRepository.saveAll(hashTags);
        
        
        // 2. 해시태그맵 저장
        List<HashTagMap> hashTagMaps = hashTagsSaved.stream()
                .map(q -> HashTagMap.builder()
                        .hashtagId(q)
                        .code(gathering)
                        .build())
                .toList();
        hashTagMapRepository.saveAll(hashTagMaps);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HashTagMap> findAllByGathering(Gathering gathering) {
        return hashTagMapRepository.findAllByCode(gathering);
    }
}
