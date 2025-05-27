package com.harbaoui.iot.user_service.service;


import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mail.javamail.JavaMailSender;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MailServiceTest {

    private JavaMailSender mailSender;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        mailService = new MailService(mailSender);
    }

    @Test
    void shouldSendVerificationEmail() throws MessagingException {
        // Given
        String to = "test@example.com";
        String link = "http://localhost:8080/verify?token=abc123";

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        // When
        mailService.sendVerificationEmail(to, link);

        // Then
        verify(mailSender, times(1)).send(mockMessage);
    }

    // Removed duplicate declaration of mailSender

    @Test
    void shouldThrowExceptionWhenSendFails() throws MessagingException {
        // Arrange
        MailService mailService = new MailService(mailSender);

        // Mock the behavior to throw a MessagingException
        doThrow(MessagingException.class).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(MessagingException.class, () -> {
            mailService.sendVerificationEmail("test@example.com", "verificationLink");
        });
    }

}

