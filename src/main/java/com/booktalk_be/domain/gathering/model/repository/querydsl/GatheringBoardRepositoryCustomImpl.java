package com.booktalk_be.domain.gathering.model.repository.querydsl;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.gathering.model.repository.GatheringBoardRepositoryCustom;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardPostDetailResponse;
import com.booktalk_be.domain.gathering.responseDto.GatheringBoardResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAQueryBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static com.booktalk_be.domain.gathering.model.entity.QGatheringBoard.gatheringBoard;

public class GatheringBoardRepositoryCustomImpl extends Querydsl4RepositorySupport implements GatheringBoardRepositoryCustom {

    protected GatheringBoardRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }

    @Override
    public Page<GatheringBoardResponse> findBoardsForPaging(String gatheringCode, Pageable pageable) {

        JPAQueryBase<GatheringBoardResponse, JPAQuery<GatheringBoardResponse>> baseQuery =
                select(Projections.fields(GatheringBoardResponse.class,
                        gatheringBoard.code.as("boardCode"),
                        gatheringBoard.title,
                        gatheringBoard.delYn,
                        gatheringBoard.delReason.as("deleteReason"),
                        gatheringBoard.member.name.as("author"),
                        Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", gatheringBoard.regTime).as("date"),
                        gatheringBoard.views
                ))
                        .from(gatheringBoard)
                        .leftJoin(gatheringBoard.member)
                        .where(gatheringBoard.gathering.code.eq(gatheringCode));

        JPAQueryBase<Long, JPAQuery<Long>> countQuery =
                select(Wildcard.count)
                        .from(gatheringBoard)
                        .where(gatheringBoard.gathering.code.eq(gatheringCode));

        var content = baseQuery
                .orderBy(gatheringBoard.code.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public GatheringBoardPostDetailResponse getBoardDetailBy(String postCode) {
        return select(Projections.fields(GatheringBoardPostDetailResponse.class,
                gatheringBoard.code.as("boardCode"),
                gatheringBoard.gathering.code.as("gatheringCode"),
                gatheringBoard.title,
                gatheringBoard.content,
                gatheringBoard.member.name.as("author"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", gatheringBoard.regTime).as("date"),
                gatheringBoard.views,
                gatheringBoard.likesCnt,
                gatheringBoard.notificationYn,
                gatheringBoard.delYn,
                gatheringBoard.delReason.as("deleteReason")
        ))
                .from(gatheringBoard)
                .leftJoin(gatheringBoard.member)
                .where(gatheringBoard.code.eq(postCode))
                .fetchOne();
    }
}