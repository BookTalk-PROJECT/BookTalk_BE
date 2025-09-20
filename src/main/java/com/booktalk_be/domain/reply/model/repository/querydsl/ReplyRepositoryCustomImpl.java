package com.booktalk_be.domain.reply.model.repository.querydsl;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.board.responseDto.BoardResponse;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.model.repository.ReplyRepositoryCustom;
import com.booktalk_be.domain.reply.responseDto.ReplySimpleResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import static com.booktalk_be.domain.reply.model.entity.QReply.reply;

public class ReplyRepositoryCustomImpl extends Querydsl4RepositorySupport implements ReplyRepositoryCustom {
    protected ReplyRepositoryCustomImpl(JPAQueryFactory queryFactory) {super(queryFactory);}

    @Override
    public List<Reply> getRepliesByPostCode(String postCode) {

        return selectFrom(reply)
                .where(reply.postCode.eq(postCode))
                .where(reply.delYn.eq(false))
                .fetch();

    }

    @Override
    public Page<ReplySimpleResponse> getAllRepliesForPaging(Pageable pageable) {
        List<ReplySimpleResponse> content = select(Projections.fields(ReplySimpleResponse.class,
                reply.replyCode,
                reply.postCode,
//                reply.member.memberId,
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", reply.updateTime).as("date"),
                reply.content,
                reply.delYn,
                reply.delReason.as("deleteReason")))
//                .from(board).innerJoin(board.member)
                .from(reply)
                .orderBy(reply.replyCode.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = Optional.ofNullable(
                        select(Wildcard.count)
                                .from(reply)
//                        .innerJoin(board.member)
                                .fetchOne())
                .orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }
}
