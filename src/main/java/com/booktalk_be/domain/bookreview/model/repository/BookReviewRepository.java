package com.booktalk_be.domain.bookreview.model.repository;

import com.booktalk_be.domain.bookreview.model.entity.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookReviewRepository extends JpaRepository<BookReview, String>, BookReviewRepositoryCustom {
}
