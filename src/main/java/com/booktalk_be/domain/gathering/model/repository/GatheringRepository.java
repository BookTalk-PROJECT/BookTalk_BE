package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GatheringRepository extends JpaRepository<Gathering, String>, GatheringRepositoryCustom{
    Optional<Gathering> findByCodeAndDelYnFalse(String code);

    @Modifying
    @Query("UPDATE Gathering g SET g.delYn = true, g.delReason = :reason WHERE g.code = :code AND g.delYn = false")
    int softDelete(@Param("code") String code, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE Gathering g SET g.delYn = false, g.delReason = :reason WHERE g.code = :code AND g.delYn = true")
    int restore(@Param("code") String code, @Param("reason") String reason);

    @Query(value = "CALL sp_mypage_gathering_list(:memberId, :pageNum, :pageSize)", nativeQuery = true)
    List<Object[]> callMyGatheringList(
            @Param("memberId") int memberId,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );

    @Query(value = "CALL sp_mypage_gathering_search(:memberId, :keywordType, :keyword, :startDate, :endDate, :pageNum, :pageSize)", nativeQuery = true)
    List<Object[]> callMyGatheringSearch(
            @Param("memberId") int memberId,
            @Param("keywordType") String keywordType,
            @Param("keyword") String keyword,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );
}
