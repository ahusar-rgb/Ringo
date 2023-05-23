package com.ringo.service.common;

import com.ringo.exception.InternalException;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    public void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new InternalException("Failed to send email to %s".formatted(to));
        }
    }

    public void sendTicket(String to, String subject, String text, BufferedImage qr) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(qr, "jpeg", stream);
            DataSource ds = new ByteArrayDataSource(stream.toByteArray(), "image/jpeg");
            helper.addAttachment("qr.jpeg", ds);

            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            throw new InternalException("Failed to send email to %s".formatted(to));
        }
    }

}
