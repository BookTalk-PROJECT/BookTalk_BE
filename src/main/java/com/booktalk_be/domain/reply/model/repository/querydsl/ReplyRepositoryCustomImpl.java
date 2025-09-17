package com.booktalk_be.domain.reply.model.repository.querydsl;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.model.repository.ReplyRepositoryCustom;
import com.booktalk_be.domain.reply.responseDto.ReplyResponse;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.ArrayList;
import java.util.List;
import static com.booktalk_be.domain.board.model.entity.QBoard.board;
import static com.booktalk_be.domain.reply.model.entity.QReply.reply;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.dsl.Expressions.constant;

public class ReplyRepositoryCustomImpl extends Querydsl4RepositorySupport implements ReplyRepositoryCustom {
    protected ReplyRepositoryCustomImpl(JPAQueryFactory queryFactory) {super(queryFactory);}

    @Override
    public List<Reply> getRepliesByPostCode(String postCode) {

        return selectFrom(reply)
                .where(reply.postCode.eq(postCode))
                .where(reply.delYn.eq(false))
                .fetch();

    }
}
