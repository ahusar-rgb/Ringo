package com.ringo.controller;

import com.ringo.auth.JwtService;
import com.ringo.dto.security.UserRequestDto;
import com.ringo.dto.security.UserResponseDto;
import com.ringo.exception.AuthenticationException;
import com.ringo.model.security.User;
import com.ringo.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> login(@RequestBody UserRequestDto login) {

            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));

            if (authentication == null || !authentication.isAuthenticated()) {
                throw new AuthenticationException("User [username: %s] is not authenticated".formatted(login.getUsername()));
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) authentication.getPrincipal();
            log.info("User [username: %s] is authenticated", user.getUsername());

            return ResponseEntity
                    .ok(jwtService.generateToken(user));
    }

    @PostMapping(value = "/sign-up", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserResponseDto> signUp(@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity
                .ok(userService.saveUser(userRequestDto));
    }
}
