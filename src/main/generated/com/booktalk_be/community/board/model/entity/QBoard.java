package com.booktalk_be.community.board.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoard is a Querydsl query type for Board
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoard extends EntityPathBase<Board> {

    private static final long serialVersionUID = 919769595L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoard board = new QBoard("board");

    public final com.booktalk_be.common.entity.QPost _super;

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    //inherited
    public final StringPath code;

    //inherited
    public final StringPath content;

    //inherited
    public final StringPath createdBy;

    //inherited
    public final StringPath delReason;

    //inherited
    public final BooleanPath delYn;

    //inherited
    public final NumberPath<Integer> likeCnt;

    // inherited
    public final com.booktalk_be.auth.model.entity.QMember member;

    //inherited
    public final StringPath modifiedBy;

    //inherited
    public final BooleanPath notificationYn;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regTime;

    //inherited
    public final StringPath title;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime;

    //inherited
    public final NumberPath<Integer> views;

    public QBoard(String variable) {
        this(Board.class, forVariable(variable), INITS);
    }

    public QBoard(Path<? extends Board> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoard(PathMetadata metadata, PathInits inits) {
        this(Board.class, metadata, inits);
    }

    public QBoard(Class<? extends Board> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new com.booktalk_be.common.entity.QPost(type, metadata, inits);
        this.code = _super.code;
        this.content = _super.content;
        this.createdBy = _super.createdBy;
        this.delReason = _super.delReason;
        this.delYn = _super.delYn;
        this.likeCnt = _super.likeCnt;
        this.member = _super.member;
        this.modifiedBy = _super.modifiedBy;
        this.notificationYn = _super.notificationYn;
        this.regTime = _super.regTime;
        this.title = _super.title;
        this.updateTime = _super.updateTime;
        this.views = _super.views;
    }

}

