package com.booktalk_be.domain.reply.model.repository;

import com.booktalk_be.domain.reply.model.entity.Reply;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, String>, ReplyRepositoryCustom {
    List<Reply> findAllByPostCode(@NotNull String postCode);
}
