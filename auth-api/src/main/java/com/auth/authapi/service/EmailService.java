package com.auth.authapi.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String verificationUrl)
            throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String subject = "Email Verification";

        String content = """
                <html>
                <body>
                    <h2>Welcome to AuthSystem!</h2>
                    <p>Thank you for registering.</p>
                    <p>Click the link below to verify your email:</p>
                    <a href="%s">Verify Email</a>
                    <p>If you did not create this account, ignore this email.</p>
                </body>
                </html>
                """.formatted(verificationUrl);

        helper.setFrom(
                "authentication.sys.support@gmail.com",
                "AuthSystem"
        );
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }
}
