package com.booktalk_be.domain.gathering.model.repository;

import com.booktalk_be.domain.gathering.model.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GatheringRepository extends JpaRepository<Gathering, String>, GatheringRepositoryCustom{
    Optional<Gathering> findByCodeAndDelYnFalse(String code);
}
