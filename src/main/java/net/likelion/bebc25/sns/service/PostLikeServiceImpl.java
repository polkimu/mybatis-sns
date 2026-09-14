package net.likelion.bebc25.sns.service;

import net.likelion.bebc25.sns.dto.LikeToggleResponse;
import net.likelion.bebc25.sns.dto.PostResponse;
import net.likelion.bebc25.sns.mapper.PostLikeMapper;
import net.likelion.bebc25.sns.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class PostLikeServiceImpl implements PostLikeService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;

    public PostLikeServiceImpl(PostMapper postMapper, PostLikeMapper postLikeMapper) {
        this.postMapper = postMapper;
        this.postLikeMapper = postLikeMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeToggleResponse toggleLike(Long memberId, Long postId) {
        // 1. 대상 게시글 존재 여부 검증 (PostMapper)
        PostResponse post = postMapper.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다. ID: " + postId);
        }

        // 2. 현재 회원의 좋아요 등록 여부 확인 (PostLikeMapper)
        boolean isLiked = postLikeMapper.countLike(memberId, postId) > 0;

        if (isLiked) {
            // 기등록 상태: 좋아요 취소 (삭제) 및 like_count 1 감소
            postLikeMapper.deleteLike(memberId, postId);
            postMapper.decreaseLikeCount(postId);
            return new LikeToggleResponse(false, post.likeCount() - 1);
        } else {
            // 미등록 상태: 좋아요 등록 및 like_count 1 증가
            postLikeMapper.insertLike(memberId, postId);
            postMapper.increaseLikeCount(postId);
            return new LikeToggleResponse(true, post.likeCount() + 1);
        }
    }
}