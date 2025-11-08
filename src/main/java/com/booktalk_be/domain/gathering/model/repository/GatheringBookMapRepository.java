package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import com.booktalk_be.domain.gathering.model.entity.GatheringBookId;
import com.booktalk_be.domain.gathering.model.entity.GatheringBookMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GatheringBookMapRepository extends JpaRepository<GatheringBookMap, GatheringBookId> {

    @Query("""
        select gb
        from GatheringBookMap gb
        where gb.code.code = :code
        order by gb.order asc
    """)
    List<GatheringBookMap> findAllByGatheringCode(@Param("code") String gatheringCode);

    List<GatheringBookMap> findAllByCode(Gathering code);
}
