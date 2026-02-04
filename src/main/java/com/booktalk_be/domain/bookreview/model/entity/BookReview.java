package com.booktalk_be.domain.bookreview.model.entity;

import com.booktalk_be.common.entity.Post;
import com.booktalk_be.domain.bookreview.command.UpdateBookReviewCommand;
import com.booktalk_be.domain.member.model.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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

    @Transient
    private static com.booktalk_be.common.utils.DistributedIdGenerator idGenerator;

    public static void setIdGenerator(com.booktalk_be.common.utils.DistributedIdGenerator generator) {
        idGenerator = generator;
    }

    @PrePersist
    public void generateId() {
        if (this.code == null) {
            if (idGenerator != null) {
                this.code = idGenerator.generateBookReviewId();
            } else {
                this.code = "BR_" + System.currentTimeMillis();
            }
        }
        if (this.delYn == null) {
            this.delYn = false;
        }
        if (this.likesCnt == null) {
            this.likesCnt = 0;
        }
    }

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "book_title", nullable = false)
    private String bookTitle;

    @Column(name = "authors", nullable = false)
    private String authors;

    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Column(name = "isbn", nullable = false)
    private String isbn;

    @Column(name = "thumbnail_url", length = 5000)
    private String thumbnailUrl;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Builder
    public BookReview(Integer categoryId, Member member, String title, String content, String bookTitle, String authors,
                      String publisher, String isbn, String thumbnailUrl, Integer rating) {
        this.categoryId = categoryId;
        this.member = member;
        this.title = title;
        this.content = content;
        this.bookTitle = bookTitle;
        this.authors = authors;
        this.publisher = publisher;
        this.isbn = isbn;
        this.thumbnailUrl = thumbnailUrl;
        this.rating = rating;
        this.notificationYn = false;
    }

    public void modify(UpdateBookReviewCommand req) {
        this.title = req.getTitle();
        this.content = req.getContent();
        this.bookTitle = req.getBookTitle();
        this.authors = req.getAuthors();
        this.publisher = req.getPublisher();
        this.isbn = req.getIsbn();
        this.thumbnailUrl = req.getThumbnailUrl();
        this.rating = req.getRating();
    }

    public void delete() {
        this.delYn = true;
    }
}
