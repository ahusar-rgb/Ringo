package com.ringo.unit;

import com.ringo.auth.AuthenticationService;
import com.ringo.auth.JwtService;
import com.ringo.auth.TokenType;
import com.ringo.dto.auth.ForgotPasswordForm;
import com.ringo.exception.AuthException;
import com.ringo.mock.model.UserMock;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private EmailSender emailSender;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void forgotPasswordSuccess() {
        //given
        User user = UserMock.getUserMock();
        //when
        when(userRepository.findActiveByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateRecoverPasswordToken(user)).thenReturn("token");
        //then
        authenticationService.forgotPassword(new ForgotPasswordForm(user.getEmail()));
        verify(emailSender, times(1)).sendRecoveredPasswordEmail(user.getEmail(), "token");
    }

    @Test
    void forgotPasswordUserNotFound() {
        //given
        ForgotPasswordForm forgotPasswordForm = new ForgotPasswordForm("email");
        //when
        when(userRepository.findActiveByEmail(forgotPasswordForm.getEmail())).thenReturn(Optional.empty());
        //then
        assertThrows(AuthException.class, () -> authenticationService.forgotPassword(forgotPasswordForm));
        verify(emailSender, never()).sendRecoveredPasswordEmail(anyString(), anyString());
    }

    @Test
    void resetPasswordSuccess() {
        //given
        User user = UserMock.getUserMock();
        final String token = "token";
        //when
        when(userRepository.findVerifiedByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.getEmailFromToken(token)).thenReturn(user.getEmail());
        when(jwtService.isTokenValid(user, token, TokenType.RECOVER)).thenReturn(true);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        //then
        authenticationService.resetPassword(token, "password");

        user.setPassword("encodedPassword");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void resetPasswordWrongToken() {
        //given
        User user = UserMock.getUserMock();
        final String token = "wrongToken";
        //when
        when(userRepository.findVerifiedByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.getEmailFromToken(token)).thenReturn(user.getEmail());
        when(jwtService.isTokenValid(user, token, TokenType.RECOVER)).thenReturn(false);
        //then
        assertThrows(AuthException.class, () -> authenticationService.resetPassword(token, "password"));
    }

    @Test
    void resetPasswordUserNotFound() {
        //given
        User user = UserMock.getUserMock();
        final String token = "wrongToken";
        //when
        when(jwtService.getEmailFromToken(token)).thenReturn(user.getEmail());
        when(userRepository.findVerifiedByEmail(user.getEmail())).thenReturn(Optional.empty());
        //then
        assertThrows(AuthException.class, () -> authenticationService.resetPassword(token, "password"));
    }

    @Test
    void verifyEmailSuccess() {
        //given
        User user = UserMock.getUserMock();
        final String token = "token";
        //when
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(jwtService.getEmailFromToken(token)).thenReturn(user.getEmail());
        when(jwtService.getUsernameFromToken(token)).thenReturn(user.getUsername());
        when(userRepository.findVerifiedByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(jwtService.isTokenValid(user, token, TokenType.EMAIL_VERIFICATION)).thenReturn(true);
        //then
        authenticationService.verifyEmail(token);

        verify(userRepository, times(1)).save(userCaptor.capture());

        User userCaptorValue = userCaptor.getValue();
        assertTrue(userCaptorValue.getEmailVerified());
        assertThat(userCaptorValue).usingRecursiveComparison().ignoringFields("emailVerified").isEqualTo(user);
    }

    @Test
    void verifyEmailUserNotFound() {
        //given
        final String token = "token";
        //when
        when(jwtService.getEmailFromToken(token)).thenReturn("email");
        when(userRepository.findVerifiedByEmail("email")).thenReturn(Optional.empty());
        //then
        assertThrows(AuthException.class, () -> authenticationService.verifyEmail(token));
    }

    @Test
    void verifyUserInvalidToken() {
        //given
        User user = UserMock.getUserMock();
        final String token = "token";
        //when
        when(jwtService.getEmailFromToken(token)).thenReturn(user.getEmail());
        when(jwtService.getUsernameFromToken(token)).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(user, token, TokenType.EMAIL_VERIFICATION)).thenReturn(false);
        //then
        assertThrows(AuthException.class, () -> authenticationService.verifyEmail(token));
    }

    @Test
    void sendVerificationEmailSuccess() {
        //given
        User user = UserMock.getUserMock();
        //when
        when(jwtService.generateEmailVerificationToken(user)).thenReturn("token");
        //then
        authenticationService.sendVerificationEmail(user);
        verify(emailSender, times(1)).sendVerificationEmail(user.getEmail(), user.getUsername(), "token");
    }
}
