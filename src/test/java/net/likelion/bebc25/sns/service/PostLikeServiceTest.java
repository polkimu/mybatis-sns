package net.likelion.bebc25.sns.service;

import net.likelion.bebc25.sns.dto.LikeToggleResponse;
import net.likelion.bebc25.sns.dto.PostResponse;
import net.likelion.bebc25.sns.mapper.PostLikeMapper;
import net.likelion.bebc25.sns.mapper.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Transactional
class PostLikeServiceTest {

    @Autowired
    private PostLikeService postLikeService;

    // 실제 PostMapper의 동작을 유지하되 특정 메서드만 모킹(Mocking)하여 강제 예외를 주입함
    @MockitoSpyBean
    private PostMapper postMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Test
    @DisplayName("좋아요 토글 등록 및 카운트 증가 테스트")
    @Transactional (propagation = Propagation.NOT_SUPPORTED)
    void toggleLikeAddTest() {
        // given: 1번 회원이 1번 게시글에 좋아요 시도 (초기 미등록 상태)
        Long memberId = 1L;
        Long postId = 1L;
        PostResponse beforePost = postMapper.findById(postId);
        int initialLikeCount = beforePost.likeCount();

        // when: 첫 번째 토글 실행 (좋아요 등록)
        LikeToggleResponse result = postLikeService.toggleLike(memberId, postId);

        // then: DTO 응답값 검증 및 DB 데이터 정합성 확인
        assertThat(result.liked()).isTrue();
        assertThat(result.likeCount()).isEqualTo(initialLikeCount + 1);
        assertThat(postLikeMapper.countLike(memberId, postId)).isEqualTo(1);
        PostResponse afterPost = postMapper.findById(postId);
        assertThat(afterPost.likeCount()).isEqualTo(initialLikeCount + 1);
    }

    @Test
    @DisplayName("좋아요 토글 취소 및 카운트 감소 테스트")
    void toggleLikeRemoveTest() {
        // given: 먼저 좋아요 등록 상태로 만듦
        Long memberId = 1L;
        Long postId = 1L;
        postLikeService.toggleLike(memberId, postId);
        PostResponse likedPost = postMapper.findById(postId);
        int currentLikeCount = likedPost.likeCount();

        // when: 두 번째 토글 실행 (좋아요 취소)
        LikeToggleResponse result = postLikeService.toggleLike(memberId, postId);

        // then: DTO 응답값 검증 및 DB 데이터 정합성 확인
        assertThat(result.liked()).isFalse();
        assertThat(result.likeCount()).isEqualTo(currentLikeCount - 1);
        assertThat(postLikeMapper.countLike(memberId, postId)).isEqualTo(0);
        PostResponse unlikedPost = postMapper.findById(postId);
        assertThat(unlikedPost.likeCount()).isEqualTo(currentLikeCount - 1);
    }

    @Test
    // NOT_SUPPORTED: 진행 중인 부모 트랜잭션을 일시 중단(Suspend)하여 서비스가 독자 트랜잭션으로 즉시 롤백되도록 격리함
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("게시글 좋아요 수 증가 중 예외 발생 시 좋아요 등록 롤백 검증")
    void toggleLikeRollbackTest() {
        // given: 1번 회원이 1번 게시글에 좋아요 등록 시도
        Long memberId = 1L;
        Long postId = 1L;

        // 좋아요가 되어 있었다면 삭제하고 시작
        postLikeMapper.deleteLike(memberId, postId);

        // postMapper.increaseLikeCount(postId); // 정상 동작

        // @MockitoSpyBean을 활용하여 2단계 실행 시점에 강제 예외 주입
        doThrow(new RuntimeException("데이터베이스 네트워크 장애 발생"))
                .when(postMapper).increaseLikeCount(postId);

        // when & then: 2단계 실행 시 예외 발생 검증
        assertThatThrownBy(() -> postLikeService.toggleLike(memberId, postId))
                .isInstanceOf(RuntimeException.class);

        // then: 1단계 작업(좋아요 등록)까지 롤백되어 DB에 0건 유지 검증
        assertThat(postLikeMapper.countLike(memberId, postId)).isEqualTo(0);
    }
}