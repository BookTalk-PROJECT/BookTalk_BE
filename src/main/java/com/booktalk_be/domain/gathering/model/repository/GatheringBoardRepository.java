package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.GatheringBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringBoardRepository extends JpaRepository<GatheringBoard, String>, GatheringBoardRepositoryCustom {
}