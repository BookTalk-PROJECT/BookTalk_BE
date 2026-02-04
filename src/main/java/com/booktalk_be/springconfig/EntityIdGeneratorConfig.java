package com.booktalk_be.springconfig;

import com.booktalk_be.common.utils.DistributedIdGenerator;
import com.booktalk_be.domain.board.model.entity.Board;
import com.booktalk_be.domain.bookreview.model.entity.BookReview;
import com.booktalk_be.domain.reply.model.entity.Reply;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes the DistributedIdGenerator for all entities that need unique ID generation.
 * This ensures consistent ID generation across all instances in a distributed environment.
 */
@Configuration
@RequiredArgsConstructor
public class EntityIdGeneratorConfig {

    private final DistributedIdGenerator idGenerator;

    @PostConstruct
    public void initializeEntityIdGenerators() {
        Board.setIdGenerator(idGenerator);
        Reply.setIdGenerator(idGenerator);
        BookReview.setIdGenerator(idGenerator);
    }
}
