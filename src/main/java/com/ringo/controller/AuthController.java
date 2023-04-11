package com.ringo.controller;

import com.ringo.auth.JwtService;
import com.ringo.config.Constants;
import com.ringo.dto.auth.TokenDto;
import com.ringo.dto.security.UserRequestDto;
import com.ringo.dto.security.UserResponseDto;
import com.ringo.exception.AuthenticationException;
import com.ringo.model.security.User;
import com.ringo.repository.UserRepository;
import com.ringo.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserService userService;

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TokenDto> login(@RequestBody UserRequestDto login) {

            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new AuthenticationException("User [username: %s] is not authenticated".formatted(login.getUsername()));
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) authentication.getPrincipal();
            log.info("User [username: %s] is authenticated", user.getUsername());

            return ResponseEntity
                    .ok()
                    .body(
                            TokenDto.builder()
                                    .accessToken(jwtService.generateAccessToken(user))
                                    .refreshToken(jwtService.generateRefreshToken(user))
                                    .build()
                    );
    }

    @PostMapping(value = "/sign-up", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserResponseDto> signUp(@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity
                .ok(userService.save(userRequestDto));
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

        String username = jwtService.getUsernameFromToken(refreshToken);

        User user = userRepository.findByUsername(username).orElseThrow(
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
}
