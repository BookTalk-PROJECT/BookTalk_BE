package com.booktalk_be.domain.hashtag.service;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.hashtag.model.entity.HashTag;
import com.booktalk_be.domain.hashtag.model.entity.HashTagMap;
import com.booktalk_be.domain.hashtag.model.repository.HashTagMapRepository;
import com.booktalk_be.domain.hashtag.model.repository.HashTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public void syncHashtags(Gathering gathering, List<String> tags) {
        // 1) 기존 매핑 조회
        List<HashTagMap> existingMaps = hashTagMapRepository.findAllByCode(gathering);

        // 현재 연결된 태그들 (normalize된 value → HashTag)
        Map<String, HashTag> current = existingMaps.stream()
                .map(HashTagMap::getHashtagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        ht -> normalize(ht.getValue()),
                        ht -> ht,
                        (a, b) -> a
                ));

        // 2) 요청으로 들어온 최종 태그 집합
        LinkedHashSet<String> desired = tags == null
                ? new LinkedHashSet<>()
                : tags.stream()
                .filter(StringUtils::hasText)
                .map(this::normalize)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 3) 추가해야 할 태그
        for (String value : desired) {
            if (current.containsKey(value)) {
                // 이미 매핑 있음 → 건너뜀
                continue;
            }

            // hash_tag 테이블에서 재사용 or 신규 생성
            HashTag ht = hashTagRepository.findByValue(value)
                    .orElseGet(() -> hashTagRepository.save(HashTag.builder().value(value).build()));

            // 매핑 테이블에 추가
            HashTagMap map = HashTagMap.builder()
                    .code(gathering)
                    .hashtagId(ht)
                    .build();

            hashTagMapRepository.save(map);
        }

        // 4) 제거해야 할 태그 매핑
        List<HashTagMap> toDelete = existingMaps.stream()
                .filter(map -> {
                    HashTag ht = map.getHashtagId();
                    if (ht == null) return true;
                    String v = normalize(ht.getValue());
                    return !desired.contains(v);   // 지금 요청에 더 이상 없으면 삭제 대상
                })
                .toList();

        if (!toDelete.isEmpty()) {
            hashTagMapRepository.deleteAll(toDelete);
        }

        // hash_tag 테이블 자체에서 "고아 태그 삭제"는 여기선 안 한다.
        // 여러 모임에서 같은 태그 재사용하니까, 실수로 지우면 피곤해진다.
    }

    private String normalize(String v) {
        return v == null ? "" : v.trim().toLowerCase();
    }
}
