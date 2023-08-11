package com.ringo.auth;

import com.ringo.config.Constants;
import com.ringo.dto.auth.ChangePasswordForm;
import com.ringo.dto.auth.ForgotPasswordForm;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.exception.AuthException;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final GoogleIdService googleIdService;
    private final AppleIdService appleIdService;
    private final UserRepository userRepository;
    private final EmailSender emailSender;

    private static final String FORGOT_PASSWORD_URL = "http://localhost:8080/api/auth/reset-password-form";
    private final PasswordEncoder passwordEncoder;

    public TokenDto login(UserRequestDto login) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
        } catch (AuthenticationException e) {
            throw new AuthException("Bad credentials");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        log.info("User [email: {}] is authenticated", user.getEmail());

        return TokenDto.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    public TokenDto loginWithGoogle(String token) {
        return loginWithIdProvider(token, googleIdService);
    }

    public TokenDto loginWithApple(String token) {
        return loginWithIdProvider(token, appleIdService);
    }

    public TokenDto refreshToken() {
        String refreshToken = (
                (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getRequest().getHeader("Authorization");

        if (refreshToken == null || !refreshToken.startsWith(Constants.TOKEN_PREFIX))
            throw new AuthException("No refresh token provided");

        refreshToken = refreshToken.substring(Constants.TOKEN_PREFIX.length());
        String email = jwtService.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User [email: %s] not found".formatted(email)));

        if(!jwtService.isTokenValid(user, refreshToken)) {
            throw new AuthException("Invalid token");
        }

        return TokenDto.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    public void forgotPassword(ForgotPasswordForm form) {
        User user = userRepository.findActiveByEmail(form.getEmail()).orElseThrow(
                () -> new NotFoundException("User not found")
        );
        String token = jwtService.generateRecoverPasswordToken(user);
        String link = FORGOT_PASSWORD_URL + "/" + user.getId() + "/" + token;
        emailSender.send(form.getEmail(), "Password Reset", "To reset your password, click here: " + link);
    }

    public void resetPassword(Long userId, String token, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new AuthException("User does not exist"));

        if(!jwtService.isTokenValid(user, token))
            throw new AuthException("Invalid token");

        if(newPassword.contains("="))
            newPassword = newPassword.split("=")[1]; //TODO: Remove this when frontend is ready
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public TokenDto changePassword(ChangePasswordForm changePasswordForm) {

        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal == null)
            throw new AuthException("User is not authenticated");

        User user = (User) principal;

        if(!passwordEncoder.matches(changePasswordForm.getPassword(), user.getPassword()))
            throw new AuthException("Wrong password");

        user.setPassword(passwordEncoder.encode(changePasswordForm.getNewPassword()));
        user = userRepository.save(user);

        return TokenDto.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    private TokenDto loginWithIdProvider(String token, IdProvider idProvider) {
        String email = idProvider.getUserFromToken(token).getEmail();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("User [email: " + email + "] not found")
        );

        return TokenDto.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    public String getResetPasswordForm(Long id, String token) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AuthException("Invalid token")
        );
        if(!jwtService.isTokenValid(user, token))
            throw new AuthException("Invalid token");

        return "<form action=\"/api/auth/reset-password/" + id + "/" + token + "\" method=\"post\">\n" +
                "    <label for=\"newPassword\">New password:</label><br>\n" +
                "    <input type=\"password\" id=\"newPassword\" name=\"newPassword\"><br>\n" +
                "    <input type=\"submit\" value=\"Submit\">\n" +
                "</form>";
    }

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
