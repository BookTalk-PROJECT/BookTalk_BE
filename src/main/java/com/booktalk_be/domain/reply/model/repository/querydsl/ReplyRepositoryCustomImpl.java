package com.booktalk_be.domain.reply.model.repository.querydsl;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.reply.model.repository.ReplyRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class ReplyRepositoryCustomImpl extends Querydsl4RepositorySupport implements ReplyRepositoryCustom {
    protected ReplyRepositoryCustomImpl(JPAQueryFactory queryFactory) {super(queryFactory);}
}
