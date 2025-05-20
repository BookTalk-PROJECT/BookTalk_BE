package com.booktalk_be.domain.board.model.repository.querydsl;

import com.booktalk_be.domain.board.model.repository.BoardRepositoryCustom;
import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class BoardRepositoryCustomImpl extends Querydsl4RepositorySupport implements BoardRepositoryCustom {

    protected BoardRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }

}
