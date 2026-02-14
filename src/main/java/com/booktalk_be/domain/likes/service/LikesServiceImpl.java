package com.booktalk_be.domain.likes.service;

import com.booktalk_be.domain.board.model.entity.Board;
import com.booktalk_be.domain.board.model.repository.BoardRepository;
import com.booktalk_be.domain.likes.model.entity.Likes;
import com.booktalk_be.domain.likes.model.entity.LikesId;
import com.booktalk_be.domain.likes.model.repository.LikesRepository;
import com.booktalk_be.domain.member.model.entity.Member;
import com.booktalk_be.domain.member.model.repository.MemberRepository;
import com.booktalk_be.domain.reply.model.entity.Reply;
import com.booktalk_be.domain.reply.model.repository.ReplyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LikesServiceImpl implements LikesService {

    private final LikesRepository likesRepository;
    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final MemberRepository memberRepository;

    @Override
    public void addLike(String code, Member member) {
        // Check if already liked
        if (likesRepository.existsByCodeAndMemberId(code, member.getMemberId())) {
            return; // Already liked, do nothing
        }

        // Get managed Member reference to avoid detached entity error
        Member managedMember = memberRepository.getReferenceById(member.getMemberId());

        // Create like
        Likes like = Likes.create(code, managedMember);
        likesRepository.save(like);

        // Update like count on the target entity
        updateLikeCount(code, true);
    }

    @Override
    public void removeLike(String code, Member member) {
        // Get managed Member reference to avoid detached entity error
        Member managedMember = memberRepository.getReferenceById(member.getMemberId());

        LikesId likesId = new LikesId(code, managedMember);
        Optional<Likes> existingLike = likesRepository.findById(likesId);

        if (existingLike.isEmpty()) {
            return; // Not liked, do nothing
        }

        // Delete like
        likesRepository.delete(existingLike.get());

        // Update like count on the target entity
        updateLikeCount(code, false);
    }

    @Override
    public boolean isLikedBy(String code, Integer memberId) {
        return likesRepository.existsByCodeAndMemberId(code, memberId);
    }

    @Override
    public long getLikeCount(String code) {
        return likesRepository.countByCode(code);
    }

    private void updateLikeCount(String code, boolean increment) {
        // Board codes start with "BO_"
        if (code.startsWith("BO_")) {
            Optional<Board> board = boardRepository.findById(code);
            board.ifPresent(b -> {
                if (increment) {
                    b.incrementLikes();
                } else {
                    b.decrementLikes();
                }
            });
        }
        // Reply codes start with "REP_"
        else if (code.startsWith("REP_")) {
            Optional<Reply> reply = replyRepository.findById(code);
            reply.ifPresent(r -> {
                if (increment) {
                    r.incrementLikes();
                } else {
                    r.decrementLikes();
                }
            });
        }
    }
}
