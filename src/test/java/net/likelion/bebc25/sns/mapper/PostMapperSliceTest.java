package net.likelion.bebc25.sns.mapper;

import net.likelion.bebc25.sns.dto.PostCreateRequest;
import net.likelion.bebc25.sns.dto.PostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 MySQL DataSource 사용
class PostMapperSliceTest {

    @Autowired
    private PostMapper postMapper;

    @Test
    @DisplayName("MyBatis 슬라이스 테스트 - 신규 게시글 등록 및 조회")
    void sliceTestSaveAndFind() {
        // given
        PostCreateRequest newPost = new PostCreateRequest(1L, "MyBatis 슬라이스 테스트 게시글", "https://image.com/slice.jpg");

        // when
        postMapper.save(newPost);
        List<PostResponse> posts = postMapper.findByMemberId(1L);

        // then
        assertThat(posts).isNotEmpty();
        PostResponse latestPost = posts.getFirst();
        assertThat(latestPost.content()).isEqualTo("MyBatis 슬라이스 테스트 게시글");
        assertThat(latestPost.memberId()).isEqualTo(1L);
    }
}