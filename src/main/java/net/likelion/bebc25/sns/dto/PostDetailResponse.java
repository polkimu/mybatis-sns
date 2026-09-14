package net.likelion.bebc25.sns.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDetailResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private MemberResponse author;

    // 댓글이 없는 경우 NullPointerException 방지 및 빌더 패턴 사용 시 빈 리스트 유지를 위한 기본값 설정
    @Builder.Default
    private List<CommentResponse> comments = new ArrayList<>();
}