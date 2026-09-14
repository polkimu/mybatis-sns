package net.likelion.bebc25.sns.security.oauth;

import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.sns.domain.Member;
import net.likelion.bebc25.sns.mapper.MemberMapper;
import net.likelion.bebc25.sns.security.principal.CustomUserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberMapper memberMapper;

    public CustomOAuth2UserService(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        // 1. 소셜 UserInfo 엔드포인트에서 프로필 JSON 조회
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 현재 소셜 공급자 확인
        String registrationId = userRequest
                .getClientRegistration()
                .getRegistrationId();

        // 3. 플랫폼별 회원 정보 추출 및 DB 자동 가입
        Map<String, Object> attributes =
                oAuth2User.getAttributes();

        Member member =
                saveOrUpdate(registrationId, attributes);

        // 4. 통합 인증 주체 반환
        return new CustomUserDetails(member, attributes);
    }

    private Member saveOrUpdate(
            String registrationId,
            Map<String, Object> attributes
    ) {

        String email;
        String nickname;

        switch (registrationId.toLowerCase()) {

            case "google" -> {
                email = (String) attributes.get("email");
                nickname = (String) attributes.get("name");
            }

            case "kakao" -> {

                Map<String, Object> kakaoAccount =
                        (Map<String, Object>)
                                attributes.get("kakao_account");

                Map<String, Object> profile =
                        (kakaoAccount != null)
                                ? (Map<String, Object>)
                                kakaoAccount.get("profile")
                                : null;

                email =
                        (kakaoAccount != null)
                                ? (String) kakaoAccount.get("email")
                                : null;

                nickname =
                        (profile != null)
                                ? (String) profile.get("nickname")
                                : "KakaoUser";

                // 이메일이 없는 경우 가상 이메일 생성
                if (email == null || email.isBlank()) {

                    Object kakaoId = attributes.get("id");

                    email =
                            "kakao_" + kakaoId
                                    + "@kakao.social";

                    log.info(
                            "카카오 이메일 미제공 계정: 대체 가상 이메일 [{}] 생성",
                            email
                    );
                }
            }

            default ->
                    throw new OAuth2AuthenticationException(
                            "지원하지 않는 소셜 로그인 공급자입니다: "
                                    + registrationId
                    );
        }

        // 기존 회원 조회
        Member existingMember =
                memberMapper.findByEmail(email);

        if (existingMember == null) {

            Member newMember =
                    Member.builder()
                            .email(email)
                            .password("")
                            .nickname(nickname)
                            .role("ROLE_USER")
                            .build();

            memberMapper.save(newMember);

            log.info(
                    "신규 소셜 회원 DB 자동 가입 완료: ID={}, Email={}",
                    newMember.getId(),
                    newMember.getEmail()
            );

            return newMember;
        }

        return existingMember;
    }
}