package com.ringo.controller;

import com.ringo.auth.AuthenticationService;
import com.ringo.dto.auth.ChangePasswordForm;
import com.ringo.dto.auth.ForgotPasswordForm;
import com.ringo.dto.company.UserRequestDto;
import com.ringo.dto.security.IdTokenDto;
import com.ringo.dto.security.TokenDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

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
        return ResponseEntity
                .ok()
                .body(authenticationService.login(login));
    }

    @GetMapping(value = "/refresh-token", produces = "application/json")
    public ResponseEntity<TokenDto> refreshToken() {
        return ResponseEntity
                .ok()
                .body(authenticationService.refreshToken());
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
        authenticationService.forgotPassword(form);
        return ResponseEntity.ok("Password reset link was sent to your email");
    }


    @Operation(summary = "Reset password form")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Password change form",
                            content = @Content(mediaType = "text/html", schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content)
            }
    )
    @GetMapping(value = "/reset-password-form/{id}/{token}", produces = {"text/html"})
    public ResponseEntity<String> resetPasswordForm(@PathVariable("id") Long id, @PathVariable("token") String token) {
       return ResponseEntity.ok(authenticationService.getResetPasswordForm(id, token));
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
        authenticationService.resetPassword(id, token, newPassword);
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
    public ResponseEntity<TokenDto> changePassword(@RequestBody ChangePasswordForm changePasswordForm) {
        return ResponseEntity
                .ok()
                .body(authenticationService.changePassword(changePasswordForm));
    }

    @PostMapping(value = "login/google", consumes = {"application/json"}, produces = {"application/json"})
    public ResponseEntity<TokenDto> loginWithGoogle(@RequestBody IdTokenDto token) {
        return ResponseEntity
                .ok()
                .body(authenticationService.loginWithGoogle(token.getIdToken()));
    }

//    @PostMapping(value = "login/apple", consumes = {"application/json"}, produces = {"application/json"})
//    public ResponseEntity<TokenDto> loginWithApple(@RequestBody IdTokenDto token) {
//        return ResponseEntity
//                .ok()
//                .body(authService.loginWithApple(token.getIdToken()));
//    }
}
