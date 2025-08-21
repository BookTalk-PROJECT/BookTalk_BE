package com.booktalk_be.domain.category.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "category")
public class Category {

    @PrePersist
    public void init() {
        if(this.isActive == null) {
            this.isActive = true;
        }
        if(this.delYn == null) {
            this.delYn = false;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "p_category_id", nullable = true)
    private Integer pCategoryId;

    @Column(name = "del_yn", nullable = false)
    private Boolean delYn;

    public Category(String value, Integer pCategoryId) {
        this.value = value;
        this.pCategoryId = pCategoryId;
    }
}
