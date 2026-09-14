package net.likelion.bebc25.sns.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostLikeMapperTest {

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Test
    @DisplayName("좋아요 등록 및 등록 여부 조회 (countLike) 테스트")
    void insertAndCountLikeTest() {
        // given: 1번 회원이 1번 게시글에 좋아요 등록
        Long memberId = 1L;
        Long postId = 1L;

        // when: 좋아요 등록 전후 카운트 조회
        int beforeCount = postLikeMapper.countLike(memberId, postId);
        postLikeMapper.insertLike(memberId, postId);
        int afterCount = postLikeMapper.countLike(memberId, postId);

        // then: 등록 전 0, 등록 후 1 반환 검증
        assertThat(beforeCount).isEqualTo(0);
        assertThat(afterCount).isEqualTo(1);
    }

    @Test
    @DisplayName("좋아요 취소 (deleteLike) 테스트")
    void deleteLikeTest() {
        // given: 먼저 좋아요 등록
        Long memberId = 1L;
        Long postId = 1L;
        postLikeMapper.insertLike(memberId, postId);

        // when: 좋아요 취소 (삭제) 실행
        postLikeMapper.deleteLike(memberId, postId);

        // then: 삭제 후 조회 시 카운트가 0으로 반환되는지 검증
        int count = postLikeMapper.countLike(memberId, postId);
        assertThat(count).isEqualTo(0);
    }
}