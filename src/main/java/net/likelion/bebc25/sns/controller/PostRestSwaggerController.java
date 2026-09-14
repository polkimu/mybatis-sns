package net.likelion.bebc25.sns.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.likelion.bebc25.sns.dto.ApiErrorResponse;
import net.likelion.bebc25.sns.dto.PostCreateRequest;
import net.likelion.bebc25.sns.dto.PostResponse;
import net.likelion.bebc25.sns.dto.PostSearchRequest;
import net.likelion.bebc25.sns.dto.PostUpdateRequest;
import net.likelion.bebc25.sns.service.PostService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "SNS 게시글 API", description = "피드 게시글 등록, 조회, 수정, 삭제를 담당하는 REST 컨트롤러")
//@RestController
@RequestMapping("/api/v1/posts")
public class PostRestSwaggerController {

    private final PostService postService;

    public PostRestSwaggerController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "게시글 목록 조회 및 검색", description = "검색 키워드 및 정렬 조건에 부합하는 게시글 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    @GetMapping
    public ResponseEntity<List<PostResponse>> getPostList(
            @ParameterObject @ModelAttribute PostSearchRequest searchRequest) {
        return ResponseEntity.ok(postService.searchPosts(searchRequest));
    }

    @Operation(summary = "게시글 단건 상세 조회", description = "기본 키 ID에 해당하는 게시글의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostDetail(
            @Parameter(description = "조회할 게시글 ID", example = "1")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @Operation(summary = "신규 게시글 등록", description = "회원 ID와 게시글 본문, 이미지 URL을 전달받아 피드에 등록합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글 생성 성공",
                    headers = @Header(name = "Location", description = "생성된 게시글의 상세 조회 URI 경로", schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값 유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Parameter(description = "작성자 회원 ID", example = "1")
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody PostCreateRequest request) {
        request.setMemberId(memberId); // 임시 헤더의 회원 식별자를 모델(DTO)에 주입
        PostResponse createdPost = postService.createPost(request);
        URI location = URI.create("/api/v1/posts/" + createdPost.id());
        return ResponseEntity.created(location).body(createdPost);
    }

    @Operation(summary = "게시글 수정", description = "게시글 ID와 수정할 본문 내용을 전달받아 데이터를 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값 유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인 작성 게시글이 아니므로 수정 권한 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "수정할 대상 게시글이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "작성자 회원 ID", example = "1")
            @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "수정할 게시글 ID", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody PostUpdateRequest request) {
        PostResponse post = postService.getPostById(id);
        if (!post.memberId().equals(memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        postService.updatePost(id, request);
        PostResponse updatedPost = postService.getPostById(id);
        return ResponseEntity.ok(updatedPost);
    }

    @Operation(summary = "게시글 삭제", description = "게시글 ID를 전달받아 해당 자원을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "게시글 삭제 완료",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본인 작성 게시글이 아니므로 삭제 권한 없음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "삭제할 대상 게시글이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "작성자 회원 ID", example = "1")
            @RequestHeader("X-Member-Id") Long memberId,
            @Parameter(description = "삭제할 게시글 ID", example = "1")
            @PathVariable("id") Long id) {
        PostResponse post = postService.getPostById(id);
        if (!post.memberId().equals(memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}