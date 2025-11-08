package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GatheringRepository extends JpaRepository<Gathering, String>, GatheringRepositoryCustom{
    Optional<Gathering> findByCodeAndDelYnFalse(String code);

    @Modifying
    @Query("UPDATE Gathering g SET g.delYn = true, g.delReason = :reason WHERE g.code = :code AND g.delYn = false")
    int softDelete(@Param("code") String code, @Param("reason") String reason);
}
