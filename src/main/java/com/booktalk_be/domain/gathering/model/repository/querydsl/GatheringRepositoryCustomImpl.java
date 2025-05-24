package com.booktalk_be.domain.gathering.model.repository.querydsl;

import com.booktalk_be.domain.gathering.model.repository.GatheringRepositoryCustom;
import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class GatheringRepositoryCustomImpl extends Querydsl4RepositorySupport implements GatheringRepositoryCustom {
    protected GatheringRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }
}