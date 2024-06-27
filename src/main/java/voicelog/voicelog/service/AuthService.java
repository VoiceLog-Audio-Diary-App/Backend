package voicelog.voicelog.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import voicelog.voicelog.domain.EmailCertification;
import voicelog.voicelog.dto.request.*;
import voicelog.voicelog.dto.response.*;
import voicelog.voicelog.provider.EmailProvider;
import voicelog.voicelog.repository.EmailCertificationRepository;
import voicelog.voicelog.repository.UserRepository;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import voicelog.voicelog.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender javaMailSender;
    private static final String senderEmail= "lsj90954511@gmail.com";

    private final EmailProvider emailProvider;
    private final EmailCertificationRepository emailCertificationRepository;

    private final JwtUtil jwtUtil;
    // 6자리 인증코드 생성
    private String generateValidationCode() {
        Random rand = new Random();
        int number = rand.nextInt(999999);

        return String.format("%06d", number);
    }

    /*@Async
    public void sendMail(EmailCertificationRequestDto emailDto) {
        String certificationNumber = generateValidationCode();
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, emailDto.getEmail());
            message.setSubject("[VoiceLog] 이메일 인증번호 입니다.");
            String body = "";
            body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
            body += "<h1>" + certificationNumber + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body,"UTF-8", "html");

            javaMailSender.send(message);

            emailVerificationMap.put(emailDto.getEmail(), certificationNumber);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public String getVerificationNumber(String mail) {
        return emailVerificationMap.getOrDefault(mail, "None"); // 해당 이메일의 인증 번호 반환, 없으면 -1 반환
    }

    public boolean checkVerificationNumber(String mail, String userNumber) {
        String storedNumber = getVerificationNumber(mail);
        return storedNumber.equals(userNumber);
    }*/

    /*public ResponseDto signUp(SignUpRequestDto dto) {
        if (userRepository.existsByUsername(dto.getEmail())) {
            return new ResponseDto(ResponseCode.DUPLICATE_EMAIL, ResponseMessage.DUPLICATE_EMAIL);
        }

        String password = dto.getPassword();
        password = passwordEncoder.encode(password);
        dto.setPassword(password);

        User user = new User(dto);

        try{

            userRepository.save(user);
        } catch (Exception e) {
            return new ResponseDto(ResponseCode.DATABASE_ERROR, ResponseMessage.DATABASE_ERROR);
        }
        return new ResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
    }*/
    //회원가입
    public ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto) {
        try {
            String email = dto.getEmail();
            String certificationNumber = dto.getCertificationNumber();

            boolean isExist = userRepository.existsByUsername(email);
            if (isExist)
                return SignUpResponseDto.duplicatedEmail();

            EmailCertification emailCertification = emailCertificationRepository.findByEmail(email);
            boolean isMatched = emailCertification.getEmail().equals(email) && emailCertification.getCertificationNumber().equals(certificationNumber);

            if (!isMatched)
                return SignUpResponseDto.certificationFail();

            String password = dto.getPassword();
            password = passwordEncoder.encode(password);
            dto.setPassword(password);

            User user = new User(dto);
            userRepository.save(user);

            emailCertificationRepository.deleteByEmail(email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        return SignUpResponseDto.success();
    }

    //이메일 중복 체크
    public ResponseEntity<? super EmailCheckResponseDto> emailCheck(EmailCheckRequestDto dto) {
        try {
            String email = dto.getEmail();
            boolean isExist = userRepository.existsByUsername(email);
            if (isExist)
                return EmailCheckResponseDto.duplicatedEmail();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return EmailCheckResponseDto.success();
    }

    //이메일 보내기
    public ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto) {
        try {
            String email = dto.getEmail();
            boolean isExist = userRepository.existsByUsername(email);
            if (isExist)
                return EmailCheckResponseDto.duplicatedEmail();

            String certificationNumber = generateValidationCode();

            boolean isSuccess = emailProvider.sendCertificationMail(email, certificationNumber);

            if (!isSuccess)
                return EmailCertificationResponseDto.mailSendFail();

            EmailCertification emailCertification = new EmailCertification(email, certificationNumber);
            emailCertificationRepository.save(emailCertification);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        return EmailCertificationResponseDto.success();
    }

    //인증번호 확인
    public ResponseEntity<? super CertificationCheckResponseDto> certificationCheck(CertificationCheckRequestDto dto) {
        try {
            String email = dto.getEmail();
            String certificationNumber = dto.getCertificationNumber();

            EmailCertification emailCertification = emailCertificationRepository.findByEmail(email);

            if (emailCertification == null)
                return CertificationCheckResponseDto.certificationFail();

            boolean isMatched = emailCertification.getEmail().equals(email) && emailCertification.getCertificationNumber().equals(certificationNumber);

            if (!isMatched)
                return CertificationCheckResponseDto.certificationFail();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        return CertificationCheckResponseDto.success();
    }

    //로그인
    public ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto) {
        String token = null;

        try {
            String email = dto.getEmail();
            User user = userRepository.findByUsername(email);
            if (user == null || user.getStatus() != 1)
                return SignInResponseDto.signInFail();

            String password = dto.getPassword();
            String encodedPassword = user.getPassword();
            boolean isMatched = passwordEncoder.matches(password, encodedPassword);
            if (!isMatched)
                return SignInResponseDto.signInFail();

            token = jwtUtil.createJwt(email, 1000 * 60 * 60L);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        System.out.println(token);
        return SignInResponseDto.success(token);
    }
}
