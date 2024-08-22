package voicelog.voicelog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import voicelog.voicelog.domain.User;
import voicelog.voicelog.dto.request.auth.PasswordCheckRequestDto;
import voicelog.voicelog.dto.request.auth.PasswordPatchRequestDto;
import voicelog.voicelog.dto.response.auth.PasswordCheckResponseDto;
import voicelog.voicelog.dto.response.auth.PasswordPatchResponseDto;
import voicelog.voicelog.dto.response.auth.SocialUserCheckResponseDto;
import voicelog.voicelog.provider.EmailProvider;
import voicelog.voicelog.repository.EmailCertificationRepository;
import voicelog.voicelog.repository.UserRepository;
import voicelog.voicelog.utils.JwtUtil;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //네이버 여부 확인
    public ResponseEntity<? super SocialUserCheckResponseDto> socialCheck(String email) {
        try {
            User user = userRepository.findByUsernameAndStatus(email, 1);

            if (user.getType().equals("naver"))
                return SocialUserCheckResponseDto.socialRequest();
            else
                return SocialUserCheckResponseDto.success();
        } catch (Exception e) {
            e.printStackTrace();
            return SocialUserCheckResponseDto.databaseError();
        }
    }

    //이전 비밀번호 확인
    public ResponseEntity<? super PasswordCheckResponseDto> oldPasswordCheck(PasswordCheckRequestDto dto, String email) {
        try {
            User user = userRepository.findByUsernameAndStatus(email, 1);

            boolean isMatched = passwordEncoder.matches(dto.getOldPassword(), user.getPassword());
            System.out.println("isMatch result: " + isMatched);
            if (!isMatched) {
                System.out.println("비번 틀림");
                return PasswordCheckResponseDto.wrongPassword();
            } else {
                System.out.println("비번 맞음");
                return PasswordCheckResponseDto.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return PasswordCheckResponseDto.databaseError();
        }
    }

    //비밀번호 재설정
    public ResponseEntity<? super PasswordPatchResponseDto> passwordPatch(PasswordPatchRequestDto dto, String email) {

        try{
            User user = userRepository.findByUsernameAndStatus(email, 1);

            if (!dto.getNewPassword().equals(dto.getCheckNewPassword())) {
                return PasswordPatchResponseDto.notEqualPassword();
            }

            boolean isMatched = passwordEncoder.matches(dto.getNewPassword(), user.getPassword());
            if (isMatched) {
                return PasswordPatchResponseDto.reusedPassword();
            }

            String password = dto.getNewPassword();
            password = passwordEncoder.encode(password);
            user.setPassword(password);
            user.setUpdated_at(LocalDateTime.now());
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            return PasswordPatchResponseDto.databaseError();
        }
        return PasswordPatchResponseDto.success();
    }
}
