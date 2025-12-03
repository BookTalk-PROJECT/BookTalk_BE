package com.booktalk_be.domain.bookreview.repository.querydsl;

import com.booktalk_be.common.utils.Querydsl4RepositorySupport;
import com.booktalk_be.domain.bookreview.dto.BookReviewListDto;
import com.booktalk_be.domain.bookreview.dto.BookReviewSearchCondCommand;
import com.booktalk_be.domain.bookreview.repository.BookReviewRepositoryCustom;
import com.booktalk_be.common.command.PostSearchCondCommand; // Corrected import
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery; // Corrected import
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.booktalk_be.domain.bookreview.entity.QBookReview.bookReview;

public class BookReviewRepositoryCustomImpl extends Querydsl4RepositorySupport implements BookReviewRepositoryCustom {

    public BookReviewRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        super(queryFactory);
    }

    @Override
    public Page<BookReviewListDto> findByCondition(Pageable pageable) {
        List<BookReviewListDto> content = select(Projections.fields(BookReviewListDto.class,
                bookReview.code.as("code"),
                bookReview.bookTitle.as("bookTitle"),
                bookReview.title.as("reviewTitle"),
                bookReview.member.name.as("author"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", bookReview.regTime).as("publicationDate"),
                bookReview.rating.as("rating"),
                bookReview.thumbnail.as("thumbnailUrl")))
                .from(bookReview)
                .leftJoin(bookReview.member)
                .where(bookReview.delYn.eq(false))
                .orderBy(bookReview.code.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                        select(Wildcard.count)
                                .from(bookReview)
                                .where(bookReview.delYn.eq(false))
                                .fetchOne())
                .orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<BookReviewListDto> searchByCondition(BookReviewSearchCondCommand cmd, Pageable pageable) {
        JPAQuery<BookReviewListDto> baseQuery = select(Projections.fields(BookReviewListDto.class,
                bookReview.code.as("code"),
                bookReview.bookTitle.as("bookTitle"),
                bookReview.title.as("reviewTitle"),
                bookReview.member.name.as("author"),
                Expressions.stringTemplate("DATE_FORMAT({0}, '%Y-%m-%d')", bookReview.regTime).as("publicationDate"),
                bookReview.rating.as("rating"),
                bookReview.thumbnail.as("thumbnailUrl")))
                .from(bookReview)
                .leftJoin(bookReview.member)
                .where(bookReview.delYn.eq(false));

        JPAQuery<Long> countQuery = select(Wildcard.count)
                .from(bookReview)
                .leftJoin(bookReview.member)
                .where(bookReview.delYn.eq(false));

        BooleanBuilder searchCondition = new BooleanBuilder()
                .and(keywordFilter(cmd.getType(), cmd.getKeyword())) // Corrected
                .and(dateFilter(cmd.getStartDate(), cmd.getEndDate()));

        List<BookReviewListDto> content = baseQuery
                .where(searchCondition)
                .orderBy(bookReview.code.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(
                countQuery
                        .where(searchCondition)
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression keywordFilter(PostSearchCondCommand.PostKeywordType type, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        return switch (type) {
            case TITLE -> bookReview.title.containsIgnoreCase(keyword);
            case AUTHOR -> bookReview.member.name.containsIgnoreCase(keyword);
            // Assuming you want to search by book title and ISBN as well
            case BOOK_TITLE -> bookReview.bookTitle.containsIgnoreCase(keyword);
            case ISBN -> bookReview.isbn.containsIgnoreCase(keyword);
            default -> null; // BOOK_CODE as default
        };
    }

    private BooleanExpression dateFilter(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return bookReview.regTime.between(
                    startDate.atStartOfDay(),
                    endDate.atTime(LocalTime.MAX)
            );
        }
        if (startDate != null) {
            return bookReview.regTime.goe(startDate.atStartOfDay());
        }
        if (endDate != null) {
            return bookReview.regTime.loe(endDate.atTime(LocalTime.MAX));
        }
        return null;
    }
}
