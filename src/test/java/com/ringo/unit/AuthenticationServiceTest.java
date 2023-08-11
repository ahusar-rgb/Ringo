package com.ringo.unit;

import com.ringo.auth.AuthenticationService;
import com.ringo.auth.JwtService;
import com.ringo.dto.auth.ForgotPasswordForm;
import com.ringo.exception.AuthException;
import com.ringo.exception.NotFoundException;
import com.ringo.mock.model.UserMock;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.EmailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void forgotPasswordSuccess() {
        //given
        User user = UserMock.getUserMock();
        //when
        when(userRepository.findActiveByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateRecoverPasswordToken(user)).thenReturn("token");
        //then
        authenticationService.forgotPassword(new ForgotPasswordForm(user.getEmail()));
        verify(emailSender, times(1)).send(user.getEmail(), "Password Reset", "To reset your password, click here: http://localhost:8080/api/auth/reset-password-form/" + user.getId() + "/token");
    }

    @Test
    void forgotPasswordUserNotFound() {
        //given
        ForgotPasswordForm forgotPasswordForm = new ForgotPasswordForm("email");
        //when
        when(userRepository.findActiveByEmail(forgotPasswordForm.getEmail())).thenReturn(Optional.empty());
        //then
        assertThrows(NotFoundException.class, () -> authenticationService.forgotPassword(forgotPasswordForm));
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void resetPasswordSuccess() {
        //given
        User user = UserMock.getUserMock();
        final String token = "token";
        //when
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(user, token)).thenReturn(true);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        //then
        authenticationService.resetPassword(user.getId(), token, "password");

        user.setPassword("encodedPassword");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void resetPasswordWrongToken() {
        //given
        User user = UserMock.getUserMock();
        final String token = "wrongToken";
        //when
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(user, token)).thenReturn(false);
        //then
        assertThrows(AuthException.class, () -> authenticationService.resetPassword(user.getId(), token, "password"));
    }

    @Test
    void resetPasswordUserNotFound() {
        //given
        User user = UserMock.getUserMock();
        final String token = "wrongToken";
        //when
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        //then
        assertThrows(AuthException.class, () -> authenticationService.resetPassword(user.getId(), token, "password"));
    }
}
