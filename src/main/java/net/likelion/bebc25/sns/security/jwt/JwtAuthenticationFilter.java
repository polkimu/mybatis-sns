package net.likelion.bebc25.sns.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.likelion.bebc25.sns.security.service.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰이 존재하고 유효한 경우
        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {

            // 3. JWT에서 회원 ID 추출
            Long memberId = jwtProvider.getMemberId(token);

            // 4. 회원 ID로 사용자 정보 조회
            UserDetails userDetails =
                    userDetailsService.loadUserById(memberId);

            // 5. Spring Security 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            // 6. SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        // 7. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // Authorization: Bearer <token>에서 실제 토큰만 추출
    private String resolveToken(HttpServletRequest request) {

        String bearerToken =
                request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken)
                && bearerToken.startsWith(BEARER_PREFIX)) {

            return bearerToken.substring(
                    BEARER_PREFIX.length()
            );
        }

        return null;
    }
}