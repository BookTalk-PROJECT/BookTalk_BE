package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<Gathering, String>, GatheringRepositoryCustom{

}
