package com.booktalk_be.domain.category.model.entity;

import com.booktalk_be.common.entity.CommonTimeEntity;
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
public class Category extends CommonTimeEntity {

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

    public Category(String value, Boolean isActive, Integer pCategoryId) {
        this.value = value;
        this.isActive = isActive;
        this.pCategoryId = pCategoryId;
    }

    public void edit(String value, Boolean isActive) {
        this.value = value;
        this.isActive = isActive;
    }

    public void delete() {
        this.delYn = true;
    }
}
