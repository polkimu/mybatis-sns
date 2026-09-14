package net.likelion.bebc25.sns.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeMapper {

    // 좋아요 등록
    void insertLike(@Param("memberId") Long memberId, @Param("postId") Long postId);

    // 좋아요 취소 (삭제)
    void deleteLike(@Param("memberId") Long memberId, @Param("postId") Long postId);

    // 특정 회원의 게시글 좋아요 등록 여부 조회 (0 또는 1 반환)
    int countLike(@Param("memberId") Long memberId, @Param("postId") Long postId);
}