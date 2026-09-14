package net.likelion.bebc25.sns.security.principal;

import net.likelion.bebc25.sns.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private final Member member;

    // 소셜 프로필 속성 보관
    private final Map<String, Object> attributes;

    // 일반 폼 로그인 및 JWT 필터용 생성자
    public CustomUserDetails(Member member) {
        this.member = member;
        this.attributes = Collections.emptyMap();
    }

    // OAuth 2.0 소셜 로그인용 생성자
    public CustomUserDetails(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
    }

    // Member 객체 반환
    public Member getMember() {
        return member;
    }

    // 회원 ID 반환
    public Long getId() {
        return member.getId();
    }

    // 권한 목록 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(member.getRole())
        );
    }

    // 암호화된 비밀번호 반환
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    // 사용자 식별자 반환
    @Override
    public String getUsername() {
        return member.getEmail();
    }

    // OAuth2User의 소셜 프로필 속성 반환
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // OAuth2User의 사용자 식별자 반환
    @Override
    public String getName() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}