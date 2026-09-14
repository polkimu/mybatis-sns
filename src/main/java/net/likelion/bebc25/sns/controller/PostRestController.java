package net.likelion.bebc25.sns.controller;

import jakarta.validation.Valid;
import net.likelion.bebc25.sns.dto.PostCreateRequest;
import net.likelion.bebc25.sns.dto.PostResponse;
import net.likelion.bebc25.sns.dto.PostSearchRequest;
import net.likelion.bebc25.sns.dto.PostUpdateRequest;
import net.likelion.bebc25.sns.security.principal.CustomUserDetails;
import net.likelion.bebc25.sns.service.PostService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostRestController {

    private final PostService postService;

    public PostRestController(PostService postService) {
        this.postService = postService;
    }

    // 목록 조회
    @GetMapping
    public ResponseEntity<List<PostResponse>> getPostList(
            @ModelAttribute PostSearchRequest searchRequest
    ) {

        List<PostResponse> posts =
                postService.searchPosts(searchRequest);

        return ResponseEntity.ok(posts);
    }

    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostDetail(
            @PathVariable("id") Long id
    ) {

        PostResponse post =
                postService.getPostById(id);

        return ResponseEntity.ok(post);
    }

    // 3. 신규 게시글 등록 (201 Created + Location 헤더)
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest request
    ) {
        System.out.println("=== 1. createPost 진입 ===");
        System.out.println("userDetails = " + userDetails);

        request.setMemberId(userDetails.getId());

        System.out.println("=== 2. memberId 주입 완료 ===");
        System.out.println("memberId = " + request.getMemberId());

        PostResponse createdPost = postService.createPost(request);

        System.out.println("=== 3. 게시글 생성 완료 ===");

        URI location = URI.create("/api/v1/posts/" + createdPost.id());
        return ResponseEntity.created(location).body(createdPost);
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        postService.updatePost(id, request);

        PostResponse updatedPost =
                postService.getPostById(id);

        return ResponseEntity.ok(updatedPost);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        postService.deletePost(id);

        return ResponseEntity.noContent().build();
    }
}