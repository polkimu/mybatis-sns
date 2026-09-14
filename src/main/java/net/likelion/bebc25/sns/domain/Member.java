package net.likelion.bebc25.sns.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    @Builder.Default
    private String role = "ROLE_USER";
    private LocalDateTime createdAt;
}