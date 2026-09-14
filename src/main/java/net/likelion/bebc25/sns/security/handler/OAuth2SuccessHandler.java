package net.likelion.bebc25.sns.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.sns.domain.Member;
import net.likelion.bebc25.sns.security.jwt.JwtProvider;
import net.likelion.bebc25.sns.security.principal.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    public OAuth2SuccessHandler(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // 1. OAuth 인증 주체에서 회원 정보 추출
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Member member = userDetails.getMember();

        log.info(
                "OAuth2 인증 성공 처리 시작: MemberId={}, Email={}",
                member.getId(),
                member.getEmail()
        );

        // 2. 자체 JWT Access Token 생성
        String accessToken = jwtProvider.createAccessToken(
                member.getId(),
                member.getEmail(),
                member.getRole()
        );

        // 3. JWT를 쿼리 파라미터에 담아 콜백 페이지 URL 구성
        String targetUrl = UriComponentsBuilder
                .fromPath("/oauth/callback.html")
                .queryParam("accessToken", accessToken)
                .build()
                .toUriString();

        log.info("정적 콜백 페이지 리다이렉트 수행: {}", targetUrl);

        // 4. 콜백 URL로 302 Redirect
        getRedirectStrategy().sendRedirect(
                request,
                response,
                targetUrl
        );
    }
}