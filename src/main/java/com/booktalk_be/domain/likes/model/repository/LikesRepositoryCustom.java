package com.booktalk_be.domain.likes.model.repository;

public interface LikesRepositoryCustom {
    boolean existsByCodeAndMemberId(String code, Integer memberId);
    long countByCode(String code);
}
