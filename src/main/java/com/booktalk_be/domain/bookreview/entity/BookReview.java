package com.booktalk_be.domain.bookreview.entity;

import com.booktalk_be.common.baseEntity.Post;
import com.booktalk_be.domain.bookreview.dto.UpdateBookReviewCommand;
import com.booktalk_be.domain.member.model.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "book_review")
public class BookReview extends Post {

    @PrePersist
    public void generateId() {
        if (this.code == null) {
            this.code = "BR_" + System.currentTimeMillis();
        }
        if (this.delYn == null) {
            this.delYn = false;
        }
    }

    @Column(name = "book_title", nullable = false)
    private String bookTitle;

    @Column(name = "authors", nullable = false)
    private String authors;

    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Column(name = "isbn", nullable = false)
    private String isbn;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Builder
    public BookReview(Member member, String title, String content, String bookTitle, String authors,
                      String publisher, String isbn, String thumbnail, Integer rating, Boolean notificationYn) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.bookTitle = bookTitle;
        this.authors = authors;
        this.publisher = publisher;
        this.isbn = isbn;
        this.thumbnail = thumbnail;
        this.rating = rating;
        this.notificationYn = notificationYn;
    }

    public void modify(UpdateBookReviewCommand req) {
        this.title = req.getTitle();
        this.content = req.getContent();
        this.bookTitle = req.getBookTitle();
        this.authors = req.getAuthors();
        this.publisher = req.getPublisher();
        this.isbn = req.getIsbn();
        this.thumbnail = req.getThumbnail();
        this.rating = req.getRating();
        this.notificationYn = req.getNotificationYn();
    }

    public void delete() {
        this.delYn = true;
    }
}
