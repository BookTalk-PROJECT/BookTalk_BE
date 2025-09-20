package com.booktalk_be.domain.gathering.model.repository.querydsl;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import com.booktalk_be.domain.gathering.model.entity.QGatheringMemberMap;
import com.booktalk_be.domain.gathering.model.repository.GatheringRepositoryCustom;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;

import com.booktalk_be.domain.gathering.responseDto.GatheringResponse;
import com.booktalk_be.domain.hashtag.model.entity.QHashTag;
import com.booktalk_be.domain.hashtag.model.entity.QHashTagMap;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.booktalk_be.domain.gathering.model.entity.QGathering;

import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.springframework.util.StringUtils;
import java.util.List;

@Repository
public class GatheringRepositoryCustomImpl extends Querydsl4RepositorySupport implements GatheringRepositoryCustom {
    protected GatheringRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }

    @Override
    public Page<GatheringResponse> findGatheringList(GatheringStatus status, String search, Pageable pageable) {
        QGathering gathering = QGathering.gathering;
        QGatheringMemberMap gatheringMemberMap = QGatheringMemberMap.gatheringMemberMap;
        QHashTagMap hashTagMap = QHashTagMap.hashTagMap;

        // 조건 구성
        BooleanBuilder condition = new BooleanBuilder();
        if (status != null) {
            condition.and(gathering.status.eq(status));
        }
        if (StringUtils.hasText(search)) {
            condition.and(gathering.name.containsIgnoreCase(search));
        }

        // 전체 개수 먼저 조회
        long total = getQueryFactory()
                .select(gathering.count())
                .from(gathering)
                .where(condition)
                .fetchOne();

        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 모임 리스트 조회 (페이징)
        List<Gathering> gatherings = getQueryFactory()
                .selectFrom(gathering)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // DTO 매핑
        List<GatheringResponse> content = gatherings.stream().map(entity -> {
            // 실제 멤버 수 조회
            long memberCount = getQueryFactory()
                    .select(gatheringMemberMap.count())
                    .from(gatheringMemberMap)
                    .where(gatheringMemberMap.code.eq(entity))
                    .fetchOne();

            // 해시태그 리스트 조회
            List<String> hashtags = getQueryFactory()
                    .select(QHashTag.hashTag.value)
                    .from(QHashTagMap.hashTagMap)
                    .join(QHashTagMap.hashTagMap.hashtagId, QHashTag.hashTag)
                    .where(QHashTagMap.hashTagMap.code.eq(entity))
                    .fetch();


            return GatheringResponse.builder()
                    .code(entity.getCode())
                    .title(entity.getName())
                    .views((int) (Math.random() * 1000)) // 임시
                    .currentMembers((int) memberCount)
                    .maxMembers(entity.getRecruitmentPersonnel())
                    .status(entity.getStatus())
                    .imageUrl(entity.getImageUrl())
                    .hashtags(hashtags) // 임시
                    .build();
        }).toList();

        return new PageImpl<>(content, pageable, total);
    }

}