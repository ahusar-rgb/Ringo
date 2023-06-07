package com.ringo.service.common;

import com.ringo.exception.InternalException;
import com.ringo.model.company.Ticket;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

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

    public void sendTicket(Ticket ticket, BufferedImage qr) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            String html = Files.readString(new File("src/main/resources/static/email-template.html").toPath());
            html = html.replace("{{ title of the event }}", ticket.getId().getEvent().getName());
            html = html.replace("{{ name }}", ticket.getId().getParticipant().getName());


            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(ticket.getId().getParticipant().getEmail());
            helper.setSubject("Yout ticket for %s".formatted(ticket.getId().getEvent().getName()));

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(qr, "jpeg", stream);
            DataSource ds = new ByteArrayDataSource(stream.toByteArray(), "image/jpeg");
            helper.addAttachment("qr.jpeg", ds);

            byte[] encoded = Base64.getEncoder().encode(stream.toByteArray());
            html = html.replace("{{ qr-code }}", new String(encoded));
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            throw new InternalException("Failed to send email to %s".formatted(ticket.getId().getParticipant().getEmail()));
        }
    }

}
