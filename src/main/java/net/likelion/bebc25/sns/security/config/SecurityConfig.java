package net.likelion.bebc25.sns.security.config;

import net.likelion.bebc25.sns.security.handler.CustomAuthenticationEntryPoint;
import net.likelion.bebc25.sns.security.handler.OAuth2SuccessHandler;
import net.likelion.bebc25.sns.security.jwt.JwtAuthenticationFilter;
import net.likelion.bebc25.sns.security.jwt.JwtProvider;
import net.likelion.bebc25.sns.security.oauth.CustomOAuth2UserService;
import net.likelion.bebc25.sns.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(
            JwtProvider jwtProvider,
            CustomUserDetailsService userDetailsService,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2SuccessHandler oAuth2SuccessHandler
    ) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)

                // JWT 무상태 인증
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // JWT 인증 필터 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(
                                jwtProvider,
                                userDetailsService
                        ),
                        UsernamePasswordAuthenticationFilter.class
                )

                // 401 인증 실패 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                customAuthenticationEntryPoint
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // 소셜 로그인 테스트용
                        .requestMatchers(
                                "/login.html",
                                "/favicon.ico",
                                "/oauth/**"
                        ).permitAll()

                        // Swagger 접근 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // 게시글 조회는 누구나 가능
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/posts/**"
                        ).permitAll()

                        // 회원 API는 인증 필요
                        .requestMatchers(
                                "/api/v1/members/**"
                        ).authenticated()

                        // 게시글 작성은 인증 필요
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/posts/**"
                        ).authenticated()

                        // 게시글 수정은 인증 필요
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/posts/**"
                        ).authenticated()

                        // 게시글 삭제는 인증 필요
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/posts/**"
                        ).authenticated()

                        // 나머지는 허용
                        .anyRequest().permitAll()
                )

                // OAuth2 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2

                        // 소셜 사용자 정보 조회 및 DB 저장
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(
                                        customOAuth2UserService
                                )
                        )

                        // OAuth2 인증 성공 후 JWT 발급
                        .successHandler(
                                oAuth2SuccessHandler
                        )
                );

        return http.build();
    }
}