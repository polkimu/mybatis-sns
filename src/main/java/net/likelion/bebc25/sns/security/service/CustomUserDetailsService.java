package net.likelion.bebc25.sns.security.service;

import net.likelion.bebc25.sns.domain.Member;
import net.likelion.bebc25.sns.mapper.MemberMapper;
import net.likelion.bebc25.sns.security.principal.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    public CustomUserDetailsService(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Member member = memberMapper.findByEmail(email);

        if (member == null) {
            throw new UsernameNotFoundException(
                    "해당 이메일을 사용하는 사용자를 찾을 수 없습니다: " + email
            );
        }

        return new CustomUserDetails(member);
    }

    // 회원 PK 기반 사용자 조회
    // JWT 토큰의 subject(sub)에 저장된 memberId로 조회
    public UserDetails loadUserById(Long id)
            throws UsernameNotFoundException {

        Member member = memberMapper.findById(id);

        if (member == null) {
            throw new UsernameNotFoundException(
                    "해당 식별자의 사용자를 찾을 수 없습니다: " + id
            );
        }

        return new CustomUserDetails(member);
    }
}