package com.booktalk_be.domain.gathering.model.repository.querydsl;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringStatus;
import com.booktalk_be.domain.gathering.model.repository.GatheringRepositoryCustom;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;

import com.booktalk_be.domain.gathering.responseDto.GatheringResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import com.booktalk_be.domain.gathering.model.entity.QGathering;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.List;

@Repository
public class GatheringRepositoryCustomImpl extends Querydsl4RepositorySupport implements GatheringRepositoryCustom {
    protected GatheringRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }

    @Override
    public Page<GatheringResponse> findGatheringList(GatheringStatus status, String search, Pageable pageable) {
        QGathering gathering = QGathering.gathering;

        BooleanBuilder builder = new BooleanBuilder();
        System.out.println("이걸 봐야한다"+ status);
        if (status != null) {
            builder.and(gathering.status.eq(status));
        }
        if (StringUtils.hasText(search)) {
            builder.and(gathering.name.containsIgnoreCase(search));
        }

        List<Gathering> gatherings = getQueryFactory()
                .selectFrom(gathering)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<GatheringResponse> results = gatherings.stream()
                .map(entity -> {
                    // recruitInfo에서 최대 인원 추출
                    String[] parts = entity.getRecruitInfo().split("_");
                    int maxMembers = 0;
                    if (parts.length > 1) {
                        try {
                            maxMembers = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            maxMembers = 0;
                        }
                    }

                    // 이미지 byte[] → Base64 인코딩
//                    String base64Image = entity.getImageData() != null
//                            ? "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(entity.getImageData())
//                            : null;

                    return GatheringResponse.builder()
                            .code(entity.getCode())
                            .title(entity.getName())
                            .views((int) (Math.random() * 1000)) //임시
                            .currentMembers((int) (Math.random() * 8) + 2) // 임시
                            .maxMembers(maxMembers)
                            .status(entity.getStatus())
//                            .imageUrl(base64Image)
                            .hashtags(List.of("#독서", "#문학", "#심리학")) // 임시
                            .build();
                })
                .toList();

        long total = getQueryFactory()
                .select(gathering.count())
                .from(gathering)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total);
    }
}