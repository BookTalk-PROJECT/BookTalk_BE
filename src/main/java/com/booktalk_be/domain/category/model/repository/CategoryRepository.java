package com.booktalk_be.domain.category.model.repository;

import com.booktalk_be.domain.category.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer>, CategoryRepositoryCustom {
}
