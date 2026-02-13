package com.booktalk_be.domain.reply.model.repository.querydsl;

import com.booktalk_be.common.command.ReplySearchCondCommand;
import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.model.repository.ReplyRepositoryCustom;
import com.booktalk_be.domain.reply.responseDto.ReplySimpleResponse;
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

import static com.booktalk_be.domain.reply.model.entity.QReply.reply;

public class ReplyRepositoryCustomImpl extends Querydsl4RepositorySupport implements ReplyRepositoryCustom {
    protected ReplyRepositoryCustomImpl(JPAQueryFactory queryFactory) {super(queryFactory);}

    @Override
    public List<Reply> getRepliesByPostCode(String postCode) {
        return selectFrom(reply)
                .leftJoin(reply.member).fetchJoin()
                .leftJoin(reply.parentReplyCode).fetchJoin()
                .where(reply.postCode.eq(postCode))
                .where(reply.delYn.eq(false))
                .orderBy(reply.regTime.asc())
                .fetch();
    }

    @Override
    public Page<ReplySimpleResponse> getAllRepliesForPaging(Pageable pageable, String postCodePrefix) {
        List<ReplySimpleResponse> content = select(Projections.fields(ReplySimpleResponse.class,
                reply.replyCode,
                reply.postCode,
                reply.member.memberId,
                reply.member.name.as("author"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", reply.regTime).as("date"),
                reply.content,
                reply.delYn,
                reply.delReason.as("deleteReason")))
                .from(reply).leftJoin(reply.member)
                .where(postCodePrefixFilter(postCodePrefix))
                .orderBy(reply.replyCode.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = Optional.ofNullable(
                select(Wildcard.count)
                .from(reply).leftJoin(reply.member)
                .where(postCodePrefixFilter(postCodePrefix))
                .fetchOne())
                .orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ReplySimpleResponse> getAllRepliesForPagingByMe(Pageable pageable, int memberId, String postCodePrefix) {
        List<ReplySimpleResponse> content =
                select(Projections.fields(ReplySimpleResponse.class,
                reply.replyCode,
                reply.postCode,
                reply.member.memberId,
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", reply.regTime).as("date"),
                reply.content,
                reply.delYn,
                reply.delReason.as("deleteReason")))
                .from(reply).innerJoin(reply.member).on(reply.member.memberId.eq(memberId))
                .where(postCodePrefixFilter(postCodePrefix))
                .orderBy(reply.replyCode.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = Optional.ofNullable(
                select(Wildcard.count)
                .from(reply).innerJoin(reply.member).on(reply.member.memberId.eq(memberId))
                .where(postCodePrefixFilter(postCodePrefix))
                .fetchOne())
                .orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ReplySimpleResponse> searchAllRepliesForPagingByMe(ReplySearchCondCommand cmd, Pageable pageable, int memberId, String postCodePrefix) {
        JPAQueryBase<ReplySimpleResponse, JPAQuery<ReplySimpleResponse>> baseQuery =
                select(Projections.fields(ReplySimpleResponse.class,
                reply.replyCode,
                reply.postCode,
                reply.member.memberId,
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", reply.regTime).as("date"),
                reply.content,
                reply.delYn,
                reply.delReason.as("deleteReason")))
                .from(reply).innerJoin(reply.member).on(reply.member.memberId.eq(memberId))
                .where(reply.delYn.eq(false))
                .where(postCodePrefixFilter(postCodePrefix));

        JPAQueryBase<Long, JPAQuery<Long>> pageQuery =
                select(Wildcard.count)
                .from(reply).innerJoin(reply.member).on(reply.member.memberId.eq(memberId))
                .where(reply.delYn.eq(false))
                .where(postCodePrefixFilter(postCodePrefix));

        BooleanBuilder searchCondition = new BooleanBuilder()
                .and(keywordFilter(cmd.getType(), cmd.getKeyword()))
                .and(dateFilter(cmd.getStartDate(), cmd.getEndDate()));

        List<ReplySimpleResponse> content =
                baseQuery
                .where(searchCondition)
                .orderBy(reply.replyCode.desc())
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
    public Page<ReplySimpleResponse> searchAllRepliesForPaging(ReplySearchCondCommand cmd, Pageable pageable, String postCodePrefix) {
        JPAQueryBase<ReplySimpleResponse, JPAQuery<ReplySimpleResponse>> baseQuery =
                select(Projections.fields(ReplySimpleResponse.class,
                        reply.replyCode,
                        reply.postCode,
                        reply.member.memberId,
                        Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", reply.regTime).as("date"),
                        reply.content,
                        reply.delYn,
                        reply.delReason.as("deleteReason")))
                        .from(reply).leftJoin(reply.member)
                        .where(postCodePrefixFilter(postCodePrefix));

        JPAQueryBase<Long, JPAQuery<Long>> pageQuery =
                select(Wildcard.count)
                .from(reply).leftJoin(reply.member)
                .where(postCodePrefixFilter(postCodePrefix));

        BooleanBuilder searchCondition = new BooleanBuilder()
                .and(keywordFilter(cmd.getType(), cmd.getKeyword()))
                .and(dateFilter(cmd.getStartDate(), cmd.getEndDate()));

        List<ReplySimpleResponse> content =
                baseQuery
                .where(searchCondition)
                .orderBy(reply.replyCode.desc())
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


    private BooleanExpression keywordFilter(ReplySearchCondCommand.KeywordType type, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        return switch (type) {
            case POST_CODE -> reply.postCode.containsIgnoreCase(keyword);
            case REPLY_CODE -> reply.replyCode.containsIgnoreCase(keyword);
            case CONTENT -> reply.content.containsIgnoreCase(keyword);
            default -> null;
        };
    }

    private BooleanExpression dateFilter(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return reply.regTime.between(
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );
        }
        if (startDate != null) {
            return reply.regTime.goe(startDate.atStartOfDay());
        }
        if (endDate != null) {
            return reply.regTime.loe(endDate.atTime(LocalTime.MAX));
        }
        return null;
    }

    private BooleanExpression postCodePrefixFilter(String postCodePrefix) {
        if (postCodePrefix == null || postCodePrefix.isEmpty()) {
            return null;
        }
        return reply.postCode.startsWith(postCodePrefix);
    }

    @Override
    public Page<Reply> getRootRepliesByPostCode(String postCode, Pageable pageable) {
        List<Reply> content = selectFrom(reply)
                .leftJoin(reply.member).fetchJoin()
                .where(reply.postCode.eq(postCode))
                .where(reply.parentReplyCode.isNull())
                .where(reply.delYn.eq(false))
                .orderBy(reply.regTime.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                select(Wildcard.count)
                        .from(reply)
                        .where(reply.postCode.eq(postCode))
                        .where(reply.parentReplyCode.isNull())
                        .where(reply.delYn.eq(false))
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Reply> getChildRepliesByParentCodes(List<String> parentCodes) {
        if (parentCodes == null || parentCodes.isEmpty()) {
            return List.of();
        }
        return selectFrom(reply)
                .leftJoin(reply.member).fetchJoin()
                .leftJoin(reply.parentReplyCode).fetchJoin()
                .where(reply.parentReplyCode.replyCode.in(parentCodes))
                .where(reply.delYn.eq(false))
                .orderBy(reply.regTime.asc())
                .fetch();
    }
}
