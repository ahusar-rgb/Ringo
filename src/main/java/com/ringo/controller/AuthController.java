package com.ringo.controller;

import com.ringo.auth.JwtService;
import com.ringo.config.Constants;
import com.ringo.dto.auth.ForgotPasswordForm;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.exception.AuthenticationException;
import com.ringo.exception.UserException;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.Objects;


@RestController
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TokenDto> login(@RequestBody UserRequestDto login) {

        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User [email: %s] is not authenticated".formatted(login.getEmail()));
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        log.info("User [email: {}] is authenticated", user.getEmail());

        return ResponseEntity
                .ok()
                .body(
                        TokenDto.builder()
                                .accessToken(jwtService.generateAccessToken(user))
                                .refreshToken(jwtService.generateRefreshToken(user))
                                .build()
                );
    }

    @GetMapping(value = "/get-token-expiration", produces = "application/json")
    public ResponseEntity<Date> getTokenExpiration() {
        String token = (
                (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getRequest().getHeader("Authorization");
        if(token == null || !token.startsWith(Constants.TOKEN_PREFIX))
            return ResponseEntity.badRequest().build();

        token = token.substring(Constants.TOKEN_PREFIX.length());

        return ResponseEntity
                .ok(jwtService.getTokenExpirationDate(token));
    }

    @GetMapping(value = "/refresh-token", produces = "application/json")
    public ResponseEntity<TokenDto> refreshToken() {
        String refreshToken = (
                (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        ).getRequest().getHeader("Authorization");
        if (refreshToken == null || !refreshToken.startsWith(Constants.TOKEN_PREFIX))
            return ResponseEntity.badRequest().build();

        refreshToken = refreshToken.substring(Constants.TOKEN_PREFIX.length());

        String email = jwtService.getEmailFromToken(refreshToken);

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AuthenticationException("Invalid token")
        );

        return ResponseEntity
                .ok()
                .body(
                        TokenDto.builder()
                                .accessToken(jwtService.generateAccessToken(user))
                                .refreshToken(jwtService.generateRefreshToken(user))
                                .build()
                );
    }

    @PostMapping(value = "/forgot-password", produces = {"application/json"})
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordForm form) {
        User user = userRepository.findByEmail(form.getEmail()).orElseThrow(
                () -> new UserException("User not found")
        );
        String token = jwtService.generateRecoverPasswordToken(user);
        String link = "http://localhost:8080/api/auth/reset-password-form/" + user.getId() + "/" + token;
        emailSender.send(form.getEmail(), "Password reset", "Click on the link to reset your password: " + link);
        return ResponseEntity.ok("Password reset link was sent to your email");
    }

    @GetMapping(value = "/reset-password-form/{id}/{token}", produces = {"text/html"})
    public ResponseEntity<String> resetPasswordForm(@PathVariable("id") Long id, @PathVariable("token") String token) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AuthenticationException("Invalid token")
        );
        if(!jwtService.isTokenValid(user, token))
            throw new AuthenticationException("Invalid token");

        return ResponseEntity.ok("<form action=\"/api/auth/reset-password/" + id + "/" + token + "\" method=\"post\">\n" +
                "    <label for=\"newPassword\">New password:</label><br>\n" +
                "    <input type=\"password\" id=\"newPassword\" name=\"newPassword\"><br>\n" +
                "    <input type=\"submit\" value=\"Submit\">\n" +
                "</form>");
    }


    @PostMapping(value = "/reset-password/{id}/{token}", produces = {"application/json"})
    public ResponseEntity<String> resetPassword(@PathVariable("id") Long id, @PathVariable("token") String token, @RequestBody String newPassword) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new AuthenticationException("Invalid token")
        );
        if(!jwtService.isTokenValid(user, token))
            throw new AuthenticationException("Invalid token");

        newPassword = newPassword.split("=")[1]; //TODO: Remove this when frontend is ready
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully");
    }
}
