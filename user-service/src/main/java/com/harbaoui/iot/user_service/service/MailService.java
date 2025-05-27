package com.harbaoui.iot.user_service.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String verificationLink) throws MessagingException {
        logger.info("Preparing to send verification email to: {}", to);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.setTo(to);
            helper.setSubject("Verify your email address");
            helper.setText("Click the link to verify your email: " + verificationLink);

            logger.debug("Sending email to: {}", to); // Debug-level log
            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send verification email to {}: {}", to, e.getMessage());
            throw e; // Re-throw the exception to handle it in the calling method
        }
    }
}
