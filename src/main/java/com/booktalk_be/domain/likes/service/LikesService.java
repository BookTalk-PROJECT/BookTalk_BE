package com.booktalk_be.domain.likes.service;

import com.booktalk_be.domain.member.model.entity.Member;

public interface LikesService {
    void addLike(String code, Member member);
    void removeLike(String code, Member member);
    boolean isLikedBy(String code, Integer memberId);
    long getLikeCount(String code);
}
