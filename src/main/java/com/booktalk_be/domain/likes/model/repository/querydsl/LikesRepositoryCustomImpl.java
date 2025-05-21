package com.booktalk_be.domain.likes.model.repository.querydsl;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.likes.model.repository.LikesRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class LikesRepositoryCustomImpl extends Querydsl4RepositorySupport implements LikesRepositoryCustom {
    protected LikesRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }
}
