package com.booktalk_be.domain.category.model.repository;

import com.booktalk_be.domain.category.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Integer>, CategoryRepositoryCustom {
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.value = :value AND (c.pCategoryId = :pCategoryId OR (c.pCategoryId IS NULL AND :pCategoryId IS NULL)) AND c.delYn = false")
    boolean existsByValueAndPCategoryIdAndDelYnFalse(@Param("value") String value, @Param("pCategoryId") Integer pCategoryId);
}
