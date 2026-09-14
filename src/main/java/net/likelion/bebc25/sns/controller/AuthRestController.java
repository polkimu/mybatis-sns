package net.likelion.bebc25.sns.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.sns.domain.Member;
import net.likelion.bebc25.sns.dto.LoginRequest;
import net.likelion.bebc25.sns.dto.RefreshTokenRequest;
import net.likelion.bebc25.sns.dto.TokenResponse;
import net.likelion.bebc25.sns.security.jwt.JwtProvider;
import net.likelion.bebc25.sns.security.principal.CustomUserDetails;
import net.likelion.bebc25.sns.security.service.CustomUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest request
    ) {

        // 1. 이메일 + 비밀번호로 인증 요청 객체 생성
        Authentication unauthenticatedToken =
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                );

        // 2. AuthenticationManager에게 인증 위임
        Authentication authentication =
                authenticationManager.authenticate(unauthenticatedToken);

        // 3. 인증된 사용자 정보 가져오기
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        // 4. 회원 정보 추출
        Long memberId = userDetails.getMember().getId();
        String email = userDetails.getUsername();
        String role = userDetails.getMember().getRole();

        // 5. JWT 생성
        String accessToken =
                jwtProvider.createAccessToken(memberId, email, role);

        String refreshToken =
                jwtProvider.createRefreshToken(memberId);

        // 6. 토큰 응답
        TokenResponse response =
                TokenResponse.of(
                        accessToken,
                        refreshToken,
                        3600L
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody @Valid RefreshTokenRequest request
    ) {

        String refreshToken = request.refreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException(
                    "유효하지 않거나 만료된 Refresh Token입니다."
            );
        }

        Long memberId = jwtProvider.getMemberId(refreshToken);

        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserById(memberId);

        Member member = userDetails.getMember();

        String newAccessToken =
                jwtProvider.createAccessToken(
                        member.getId(),
                        member.getEmail(),
                        member.getRole()
                );

        String newRefreshToken =
                jwtProvider.createRefreshToken(member.getId());

        TokenResponse response =
                TokenResponse.of(
                        newAccessToken,
                        newRefreshToken,
                        3600L
                );

        return ResponseEntity.ok(response);
    }
}