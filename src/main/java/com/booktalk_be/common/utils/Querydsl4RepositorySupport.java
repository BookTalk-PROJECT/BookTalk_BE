package com.booktalk_be.common.utils;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
public abstract class Querydsl4RepositorySupport {
    private final JPAQueryFactory queryFactory;

    protected Querydsl4RepositorySupport(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    protected JPAQueryFactory getQueryFactory() {
        return queryFactory;
    }

    protected <T> JPAQuery<T> select(Expression<T> expr) {
        return getQueryFactory().select(expr);
    }

    protected JPAQuery<Tuple> select(Expression<?>... exprs) {
        return getQueryFactory().select(exprs);
    }

    protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
        return getQueryFactory().selectFrom(from);
    }

    protected JPAQuery<Integer> selectOne() { return getQueryFactory().selectOne(); }

}