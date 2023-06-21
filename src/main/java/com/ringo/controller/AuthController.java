package com.ringo.controller;

import com.ringo.auth.GoogleIdTokenService;
import com.ringo.auth.JwtService;
import com.ringo.config.Constants;
import com.ringo.dto.auth.ChangePasswordForm;
import com.ringo.dto.auth.ForgotPasswordForm;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.security.TokenDto;
import com.ringo.exception.AuthenticationException;
import com.ringo.exception.UserException;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.EmailSender;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final GoogleIdTokenService googleIdTokenService;

    @Operation(summary = "Login")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class))),
                    @ApiResponse(responseCode = "401", description = "User is not authenticated", content = @Content)
            }
    )
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TokenDto> login(@RequestBody UserRequestDto login) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));

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

        User user = userRepository.findActiveByEmail(email).orElseThrow(
                () -> new AuthenticationException("Invalid token")
        );

        if(!jwtService.isTokenValid(user, refreshToken)) {
            throw new AuthenticationException("Invalid token");
        }

        return ResponseEntity
                .ok()
                .body(
                        TokenDto.builder()
                                .accessToken(jwtService.generateAccessToken(user))
                                .refreshToken(jwtService.generateRefreshToken(user))
                                .build()
                );
    }

    @Operation(summary = "Forgot password")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Password reset link was sent to your email",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "User not found", content = @Content)
            }
    )
    @PostMapping(value = "/forgot-password", produces = {"application/json"})
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordForm form) {
        User user = userRepository.findActiveByEmail(form.getEmail()).orElseThrow(
                () -> new UserException("User not found")
        );
        String token = jwtService.generateRecoverPasswordToken(user);
        String link = "http://localhost:8080/api/auth/reset-password-form/" + user.getId() + "/" + token;
        emailSender.send(form.getEmail(), "Password reset", "Click on the link to reset your password: " + link);
        return ResponseEntity.ok("Password reset link was sent to your email");
    }


    @Operation(summary = "Reset password form (will be moved to frontend)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Password change form",
                            content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content)
            }
    )
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


    @Operation(summary = "Reset password")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Password reset successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content)
            }
    )
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


    @Operation(summary = "Change password")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Password changed successfully",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokenDto.class))),
                @ApiResponse(responseCode = "400", description = "Wrong password", content = @Content),
                @ApiResponse(responseCode = "401", description = "User is not authenticated", content = @Content)
            }
    )
    @PostMapping (value = "change-password", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<TokenDto> updatePassword(@RequestBody ChangePasswordForm changePasswordForm) {

        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal == null)
            throw new UserException("User is not authenticated");

        User user = (User) principal;

        if(!passwordEncoder.matches(changePasswordForm.getPassword(), user.getPassword()))
            throw new UserException("Wrong password");

        user.setPassword(passwordEncoder.encode(changePasswordForm.getNewPassword()));
        user = userRepository.save(user);

        return ResponseEntity.ok(
                new TokenDto(
                        jwtService.generateAccessToken(user),
                        jwtService.generateRefreshToken(user)
                )
        );
    }

    @GetMapping("login/google")
    public ResponseEntity<TokenDto> loginGoogle(String token) {
        String email = googleIdTokenService.getUserFromToken(token).getEmail();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserException("User [email: " + email + "] not found")
        );

        return ResponseEntity.ok(
                new TokenDto(
                        jwtService.generateAccessToken(user),
                        jwtService.generateRefreshToken(user)
                )
        );
    }
}
