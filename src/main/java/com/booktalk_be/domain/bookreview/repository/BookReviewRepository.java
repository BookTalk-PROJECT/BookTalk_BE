package com.booktalk_be.domain.bookreview.repository;

import com.booktalk_be.domain.bookreview.entity.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, String>, BookReviewRepositoryCustom {
}
