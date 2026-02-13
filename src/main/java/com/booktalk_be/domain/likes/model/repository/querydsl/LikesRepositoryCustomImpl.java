package com.booktalk_be.domain.likes.model.repository.querydsl;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.likes.model.repository.LikesRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;

import static com.booktalk_be.domain.likes.model.entity.QLikes.likes;

public class LikesRepositoryCustomImpl extends Querydsl4RepositorySupport implements LikesRepositoryCustom {
    protected LikesRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }

    @Override
    public boolean existsByCodeAndMemberId(String code, Integer memberId) {
        Integer result = select(likes.code.length())
                .from(likes)
                .where(likes.code.eq(code)
                        .and(likes.member.memberId.eq(memberId)))
                .fetchFirst();
        return result != null;
    }

    @Override
    public long countByCode(String code) {
        Long count = select(likes.count())
                .from(likes)
                .where(likes.code.eq(code))
                .fetchOne();
        return count != null ? count : 0L;
    }
}
