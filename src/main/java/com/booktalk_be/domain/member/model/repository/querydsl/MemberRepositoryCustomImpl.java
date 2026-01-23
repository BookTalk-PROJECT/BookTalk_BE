package com.booktalk_be.domain.member.model.repository.querydsl;

import com.booktalk_be.common.command.MemberSearchCondCommand;
import com.booktalk_be.common.utils.Querydsl4RepositorySupport;

import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepositoryCustom;
import com.booktalk_be.domain.member.responseDto.MemberInformationResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.booktalk_be.domain.member.model.entity.QMember.member;

public class MemberRepositoryCustomImpl extends Querydsl4RepositorySupport implements MemberRepositoryCustom {

    protected MemberRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }
    /**
     * 1. 단순 전체 목록 페이징
     */
    @Override
    public Page<MemberInformationResponse> findMembersForPaging(Pageable pageable) {
        // 1. 엔티티 조회
        List<Member> members = selectFrom(member)
                .where(member.delYn.eq(false))
                .orderBy(member.memberId.desc()) // 최신 가입순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 카운트 조회
        Long total = Optional.ofNullable(
                        select(Wildcard.count)
                                .from(member)
                                .where(member.delYn.eq(false))
                                .fetchOne())
                .orElse(0L);

        // 3. 엔티티 -> DTO 변환 (DTO의 생성자 로직 활용)
        List<MemberInformationResponse> content = members.stream()
                .map(MemberInformationResponse::new)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 2. 검색 조건 포함 목록 페이징
     */
    @Override
    public Page<MemberInformationResponse> searchMembersForPaging(Pageable pageable, MemberSearchCondCommand cmd) {

        // 검색 조건 빌더 생성
        BooleanBuilder searchCondition = new BooleanBuilder()
                .and(member.delYn.eq(false)) // 기본 조건: 탈퇴하지 않은 회원
                .and(keywordFilter(cmd.getType(), cmd.getKeyword()))
                .and(dateFilter(cmd.getStartDate(), cmd.getEndDate()));

        // 1. 엔티티 조회
        List<Member> members = selectFrom(member)
                .where(searchCondition)
                .orderBy(member.memberId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 카운트 조회
        Long total = Optional.ofNullable(
                        select(Wildcard.count)
                                .from(member)
                                .where(searchCondition)
                                .fetchOne())
                .orElse(0L);

        // 3. 엔티티 -> DTO 변환
        List<MemberInformationResponse> content = members.stream()
                .map(MemberInformationResponse::new)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression keywordFilter(MemberSearchCondCommand.KeywordType type, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        // Member 필드에 맞게 Switch Case 적용
        return switch (type) {
            case NAME -> member.name.containsIgnoreCase(keyword);
            case EMAIL -> member.email.containsIgnoreCase(keyword);
            case PHONE -> member.phoneNumber.contains(keyword); // 전화번호는 보통 부분 일치
            default -> null;
        };
    }

    private BooleanExpression dateFilter(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return member.regTime.between(
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );
        }
        if (startDate != null) {
            return member.regTime.goe(startDate.atStartOfDay());
        }
        if (endDate != null) {
            return member.regTime.loe(endDate.atTime(LocalTime.MAX));
        }
        return null;
    }
    }