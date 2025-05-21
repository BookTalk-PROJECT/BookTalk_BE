package com.booktalk_be.domain.board.model.repository;

import com.booktalk_be.domain.board.model.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, String>, BoardRepositoryCustom {
}
