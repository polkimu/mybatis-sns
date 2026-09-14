package net.likelion.bebc25.sns.security.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.sns.domain.Member;
import net.likelion.bebc25.sns.dto.LoginRequest;
import net.likelion.bebc25.sns.dto.LoginResponse;
import net.likelion.bebc25.sns.mapper.MemberMapper;
import net.likelion.bebc25.sns.security.jwt.JwtProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberMapper memberMapper;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request) {

        // 1. 이메일로 회원 조회
        Member member =
                memberMapper.findByEmail(request.email());

        // 2. 회원 존재 여부 확인
        if (member == null) {
            throw new RuntimeException("존재하지 않는 회원입니다.");
        }

        // 3. 비밀번호 확인
        if (!member.getPassword()
                .equals(request.password())) {

            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 4. Access Token 생성
        String accessToken =
                jwtProvider.createAccessToken(
                        member.getId(),
                        member.getEmail(),
                        member.getRole()
                );

        // 5. Refresh Token 생성
        String refreshToken =
                jwtProvider.createRefreshToken(
                        member.getId()
                );

        // 6. 반환
        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }
}