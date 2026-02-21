package com.booktalk_be.domain.board.model.repository.querydsl;

import com.booktalk_be.common.command.PostSearchCondCommand;
import com.booktalk_be.domain.board.model.repository.BoardRepositoryCustom;
import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import com.booktalk_be.domain.board.responseDto.CommuDetailResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAQueryBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.booktalk_be.domain.board.model.entity.QBoard.board;
import static com.booktalk_be.domain.category.model.entity.QCategory.category;

public class BoardRepositoryCustomImpl extends Querydsl4RepositorySupport implements BoardRepositoryCustom {

    protected BoardRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }

    @Override
    public Page<BoardResponse> findBoardsForPaging(Integer categoryId, Pageable pageable) {
        List<BoardResponse> content = select(Projections.fields(BoardResponse.class,
                board.code.as("boardCode"),
                board.title,
                board.member.name.as("author"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", board.regTime).as("date"),
                board.views))
                .from(board).leftJoin(board.member)
                .where(board.categoryId.eq(categoryId).and(board.delYn.eq(false)))
                .orderBy(board.code.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = Optional.ofNullable(
                select(Wildcard.count)
                        .from(board)
                        .leftJoin(board.member)
                        .where(board.categoryId.eq(categoryId).and(board.delYn.eq(false)))
                        .fetchOne())
                .orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<BoardResponse> searchBoardsForPaging(Integer categoryId, Pageable pageable, PostSearchCondCommand cmd) {
        JPAQueryBase<BoardResponse, JPAQuery<BoardResponse>> baseQuery =
                select(Projections.fields(BoardResponse.class,
                board.code.as("boardCode"),
                board.title,
                board.member.name.as("author"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", board.regTime).as("date"),
                board.views))
                .from(board).leftJoin(board.member)
                .where(board.categoryId.eq(categoryId))
                .where(board.delYn.eq(false));

        JPAQuery<Long> pageQuery = select(Wildcard.count).from(board)
                .where(board.categoryId.eq(categoryId))
                .where(board.delYn.eq(false));
        if (cmd.getType() == PostSearchCondCommand.KeywordType.AUTHOR) {
            pageQuery.leftJoin(board.member);
        }

        BooleanBuilder searchCondition = new BooleanBuilder()
                .and(keywordFilter(cmd.getType(), cmd.getKeyword()))
                .and(dateFilter(cmd.getStartDate(), cmd.getEndDate()));

        List<BoardResponse> content =
                baseQuery
                .where(searchCondition)
                .orderBy(board.code.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = Optional.ofNullable(
                pageQuery
                .where(searchCondition)
                .fetchOne()
                ).orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public CommuDetailResponse getBoardDetailBy(String boardCode) {
        return select(Projections.fields(CommuDetailResponse.class,
                board.code.as("boardCode"),
                board.member.memberId.as("memberId"),
                board.title.as("title"),
                board.content.as("content"),
                board.member.name.as("author"),
                board.views.as("views"),
                board.likesCnt.as("likesCnt"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", board.regTime).as("regDate"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", board.updateTime).as("regDate"),
                //TODO 좋아요 기능 추가 후 활성화
//                Expressions.constant(false).as("isLiked"),
                board.notificationYn.as("notificationYn"),
                board.delYn.as("delYn"),
                board.delReason.as("delReason")))
                .from(board).leftJoin(board.member)
                .where(board.code.eq(boardCode))
                .fetchOne();
    }

    @Override
    public Page<BoardResponse> getAllBoardsForPaging(Pageable pageable) {
        List<BoardResponse> content = select(Projections.fields(BoardResponse.class,
                board.code.as("boardCode"),
                board.title,
                category.value.as("category"),
                board.member.name.as("author"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", board.regTime).as("date"),
                board.views,
                board.delYn,
                board.delReason.as("deleteReason")))
                .from(board)
                .leftJoin(board.member)
                .leftJoin(category).on(board.categoryId.eq(category.categoryId))
                .orderBy(board.code.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = Optional.ofNullable(
                        select(Wildcard.count)
                                .from(board)
                                .fetchOne())
                .orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<BoardResponse> searchAllBoardsForPaging(PostSearchCondCommand cmd, Pageable pageable) {
        JPAQueryBase<BoardResponse, JPAQuery<BoardResponse>> baseQuery =
                select(Projections.fields(BoardResponse.class,
                board.code.as("boardCode"),
                board.title,
                category.value.as("category"),
                board.member.name.as("author"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", board.regTime).as("date"),
                board.views,
                board.delYn,
                board.delReason.as("deleteReason")))
                .from(board)
                .leftJoin(board.member)
                .leftJoin(category).on(board.categoryId.eq(category.categoryId));

        JPAQuery<Long> pageQuery = select(Wildcard.count).from(board);
        if (cmd.getType() == PostSearchCondCommand.KeywordType.AUTHOR) {
            pageQuery.leftJoin(board.member);
        }

        BooleanBuilder searchCondition = new BooleanBuilder()
                .and(keywordFilter(cmd.getType(), cmd.getKeyword()))
                .and(dateFilter(cmd.getStartDate(), cmd.getEndDate()));

        List<BoardResponse> content =
                baseQuery
                .where(searchCondition)
                .orderBy(board.code.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                pageQuery
                .where(searchCondition)
                .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public String queryNextBoard(String boardCode, Integer categoryId) {
        return select(board.code.as("boardCode"))
                .from(board)
                .where(board.code.gt(boardCode)
                        .and(board.categoryId.eq(categoryId))
                        .and(board.delYn.eq(false)))
                .orderBy(board.code.asc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public String queryPrevBoard(String boardCode, Integer categoryId) {
        return select(board.code.as("boardCode"))
                .from(board)
                .where(board.code.lt(boardCode)
                        .and(board.categoryId.eq(categoryId))
                        .and(board.delYn.eq(false)))
                .orderBy(board.code.desc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public Page<BoardResponse> getAllBoardsForPagingByMe(Pageable pageable, int memberId) {
        List<BoardResponse> content = select(Projections.fields(BoardResponse.class,
                board.code.as("boardCode"),
                board.title,
                category.value.as("category"),
                board.member.name.as("author"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", board.regTime).as("date"),
                board.views,
                board.delYn,
                board.delReason.as("deleteReason")))
                .from(board)
                .leftJoin(board.member)
                .leftJoin(category).on(board.categoryId.eq(category.categoryId))
                .where(board.member.memberId.eq(memberId).and(board.delYn.eq(false)))
                .orderBy(board.code.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                        select(Wildcard.count)
                                .from(board).leftJoin(board.member)
                                .where(board.member.memberId.eq(memberId).and(board.delYn.eq(false)))
                                .fetchOne())
                .orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<BoardResponse> searchAllBoardsForPagingByMe(PostSearchCondCommand cmd, Pageable pageable, int memberId) {
        JPAQueryBase<BoardResponse, JPAQuery<BoardResponse>> baseQuery =
                select(Projections.fields(BoardResponse.class,
                board.code.as("boardCode"),
                board.title,
                category.value.as("category"),
                board.member.name.as("author"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", board.regTime).as("date"),
                board.views,
                board.delYn,
                board.delReason.as("deleteReason")))
                .from(board)
                .leftJoin(board.member)
                .leftJoin(category).on(board.categoryId.eq(category.categoryId))
                .where(board.member.memberId.eq(memberId))
                .where(board.delYn.eq(false));

        JPAQueryBase<Long, JPAQuery<Long>> pageQuery =
                select(Wildcard.count)
                .from(board)
                .leftJoin(board.member)
                .where(board.member.memberId.eq(memberId))
                .where(board.delYn.eq(false));

        BooleanBuilder searchCondition = new BooleanBuilder()
                .and(keywordFilter(cmd.getType(), cmd.getKeyword()))
                .and(dateFilter(cmd.getStartDate(), cmd.getEndDate()));

        List<BoardResponse> content =
                baseQuery
                .where(searchCondition)
                .orderBy(board.code.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                pageQuery
                .where(searchCondition)
                .fetchOne()
                ).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression keywordFilter(PostSearchCondCommand.KeywordType type, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        if(type == PostSearchCondCommand.KeywordType.CATEGORY) {
            List<Integer> matchedIds =
                    select(category.categoryId)
                    .from(category)
                    .where(category.value.contains(keyword))
                    .fetch();
            return board.categoryId.in(matchedIds);
        }
        return switch (type) {
            case CODE -> board.code.eq(keyword);
            case TITLE -> Expressions.numberTemplate(Double.class,
                    "function('match_against', {0}, {1})", board.title, keyword).gt(0);
            case AUTHOR -> board.member.name.contains(keyword);
            default -> null;
        };
    }

    private BooleanExpression dateFilter(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return board.regTime.between(
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );
        }
        if (startDate != null) {
            return board.regTime.goe(startDate.atStartOfDay());
        }
        if (endDate != null) {
            return board.regTime.loe(endDate.atTime(LocalTime.MAX));
        }
        return null;
    }
}
