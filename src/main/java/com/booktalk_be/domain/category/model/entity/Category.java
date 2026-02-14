package com.booktalk_be.domain.category.model.entity;

import com.booktalk_be.common.entity.CommonTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
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
        if(this.displayOrder == null) {
            this.displayOrder = 0;
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

    @ColumnDefault("0")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    public Category(String value, Boolean isActive, Integer pCategoryId, Integer displayOrder) {
        this.value = value;
        this.isActive = isActive;
        this.pCategoryId = pCategoryId;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public void edit(String value, Boolean isActive, Integer displayOrder) {
        this.value = value;
        this.isActive = isActive;
        if (displayOrder != null) {
            this.displayOrder = displayOrder;
        }
    }

    public void delete() {
        this.delYn = true;
    }
}
