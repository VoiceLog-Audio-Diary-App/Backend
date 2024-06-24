package voicelog.voicelog.provider;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import voicelog.voicelog.dto.request.EmailCertificationRequestDto;

@Component
@RequiredArgsConstructor
public class EmailProvider {

    private final JavaMailSender javaMailSender;

    private final String SUBJECT = "[VoiceLog] 이메일 인증번호입니다.";

    public boolean sendCertificationMail(String email, String certificationNumber) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

            String htmlContent = getCertificationMessage(certificationNumber);

            messageHelper.setTo(email);
            messageHelper.setSubject(SUBJECT);
            messageHelper.setText(htmlContent, true);

            javaMailSender.send(message);
        } catch (Exception exception){
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    private String getCertificationMessage (String certificationNumber) {
        String certificationMessage = "";
        certificationMessage += "<h3 style='text-align: center;'>요청하신 인증번호입니다.</h3>";
        certificationMessage += "<h1 style='text-align: center;'>인증코드 : <strong style='font-size: 32px; letter-spacing: 8px;'>" + certificationNumber + "</strong></h1>";
        return certificationMessage;

    }
}

    /*public void sendMail(EmailCertificationRequestDto emailDto) {
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
    }*/