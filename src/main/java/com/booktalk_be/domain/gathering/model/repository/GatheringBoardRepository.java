package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.GatheringBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GatheringBoardRepository extends JpaRepository<GatheringBoard, String>, GatheringBoardRepositoryCustom {

    @Query(value = "CALL sp_mypage_gathering_board_list(:memberId, :pageNum, :pageSize)", nativeQuery = true)
    List<Object[]> callMyGatheringBoardList(
            @Param("memberId") int memberId,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );

    @Query(value = "CALL sp_mypage_gathering_board_search(:memberId, :keywordType, :keyword, :startDate, :endDate, :pageNum, :pageSize)", nativeQuery = true)
    List<Object[]> callMyGatheringBoardSearch(
            @Param("memberId") int memberId,
            @Param("keywordType") String keywordType,
            @Param("keyword") String keyword,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );

}