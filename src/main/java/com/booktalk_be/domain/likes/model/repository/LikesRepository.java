package com.booktalk_be.domain.likes.model.repository;

import com.booktalk_be.domain.likes.model.entity.Likes;
import com.booktalk_be.domain.likes.model.entity.LikesId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, LikesId>, LikesRepositoryCustom {
}
