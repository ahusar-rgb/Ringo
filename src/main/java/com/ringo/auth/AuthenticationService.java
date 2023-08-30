package com.ringo.auth;

import com.ringo.config.ApplicationProperties;
import com.ringo.config.Constants;
import com.ringo.dto.auth.ChangePasswordForm;
import com.ringo.dto.auth.ForgotPasswordForm;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.exception.AuthException;
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
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    public static final String ANONYMOUS_USER = "anonymousUser";
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final GoogleIdService googleIdService;
    private final AppleIdService appleIdService;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final ApplicationProperties config;

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

        User user = userRepository.findVerifiedByEmail(email)
                .orElseThrow(() -> new AuthException("User [email: %s] not found".formatted(email)));

        if (!jwtService.isTokenValid(user, refreshToken, TokenType.REFRESH)) {
            throw new AuthException("Invalid token");
        }

        return TokenDto.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    public void forgotPassword(ForgotPasswordForm form) {
        User user = userRepository.findActiveByEmail(form.getEmail()).orElseThrow(
                () -> new AuthException("User not found")
        );
        String token = jwtService.generateRecoverPasswordToken(user);
        emailSender.sendRecoveredPasswordEmail(user.getEmail(), token);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findVerifiedByEmail(jwtService.getEmailFromToken(token)).orElseThrow(
                () -> new AuthException("User not found"));

        if (!jwtService.isTokenValid(user, token, TokenType.RECOVER))
            throw new AuthException("Invalid token");

        if (newPassword.contains("="))
            newPassword = newPassword.split("=")[1]; //TODO: Remove this when frontend is ready
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public TokenDto changePassword(ChangePasswordForm changePasswordForm) {

        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null)
            throw new AuthException("User is not authenticated");

        User user = (User) principal;

        if (!passwordEncoder.matches(changePasswordForm.getPassword(), user.getPassword()))
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
        User user = userRepository.findVerifiedByEmail(email).orElseThrow(
                () -> new AuthException("User [email: " + email + "] not found")
        );

        return TokenDto.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    public String getResetPasswordForm(String token) {
        User user = userRepository.findVerifiedByEmail(jwtService.getEmailFromToken(token))
                .orElseThrow(() -> new AuthException("User does not exist"));
        if (!jwtService.isTokenValid(user, token, TokenType.RECOVER))
            throw new AuthException("Invalid token");

        return "<form action=\"http://" + config.getDomainName() + "/api/auth/reset-password?token=" + token + "\" method=\"post\">\n" +
                "    <label for=\"newPassword\">New password:</label><br>\n" +
                "    <input type=\"password\" id=\"newPassword\" name=\"newPassword\"><br>\n" +
                "    <input type=\"submit\" value=\"Submit\">\n" +
                "</form>";
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal.equals(ANONYMOUS_USER))
            return null;
        return (User) principal;
    }

    public void verifyEmail(String token) {
        if(userRepository.findVerifiedByEmail(jwtService.getEmailFromToken(token)).isPresent())
            throw new UserException("Email is already verified");

        User user = userRepository.findByUsername(jwtService.getUsernameFromToken(token)).orElseThrow(
                () -> new AuthException("User not found")
        );

        if (!jwtService.isTokenValid(user, token, TokenType.EMAIL_VERIFICATION))
            throw new AuthException("Invalid token");

        user.setEmailVerified(true);
        if(!user.getWithIdProvider())
            user.setIsActive(true);
        userRepository.save(user);
    }

    public User requestVerificationEmail(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UserException("User not found")
        );

        if (user.getEmailVerified())
            throw new UserException("Email is already verified");

        sendVerificationEmail(user);

        return user;
    }

    public void sendVerificationEmail(User user) {
        String verificationToken = jwtService.generateEmailVerificationToken(user);
        emailSender.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationToken);
    }
}
