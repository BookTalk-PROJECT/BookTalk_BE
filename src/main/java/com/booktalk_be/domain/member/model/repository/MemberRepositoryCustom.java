package com.booktalk_be.domain.member.model.repository;

import com.booktalk_be.common.command.MemberSearchCondCommand;
import com.booktalk_be.domain.member.responseDto.MemberInformationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    // 1. 단순 전체 목록 페이징 (검색 조건 X)
    Page<MemberInformationResponse> findMembersForPaging(Pageable pageable);

    // 2. 검색 조건 포함 목록 페이징
    Page<MemberInformationResponse> searchMembersForPaging(Pageable pageable, MemberSearchCondCommand cmd);
}
