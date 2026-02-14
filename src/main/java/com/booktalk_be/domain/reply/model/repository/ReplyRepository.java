package com.booktalk_be.domain.reply.model.repository;

import com.booktalk_be.domain.reply.model.entity.Reply;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, String>, ReplyRepositoryCustom {
    List<Reply> findAllByPostCode(@NotNull String postCode);

    /**
     * 해당 게시글의 전체 댓글 수 (대댓글 포함)
     */
    long countByPostCodeAndDelYnFalse(String postCode);


    @Query(value = "CALL sp_mypage_gathering_reply_list(:memberId, :pageNum, :pageSize)", nativeQuery = true)
    List<Object[]> callMyGatheringReplyList(
            @Param("memberId") int memberId,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );

    @Query(value = "CALL sp_mypage_gathering_reply_search(:memberId, :keywordType, :keyword, :startDate, :endDate, :pageNum, :pageSize)", nativeQuery = true)
    List<Object[]> callMyGatheringReplySearch(
            @Param("memberId") int memberId,
            @Param("keywordType") String keywordType,
            @Param("keyword") String keyword,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize
    );
}
