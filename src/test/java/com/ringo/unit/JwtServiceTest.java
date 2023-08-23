package com.ringo.unit;

import com.ringo.auth.AuthenticationProperties;
import com.ringo.auth.JwtService;
import com.ringo.auth.TokenType;
import com.ringo.mock.model.TicketMock;
import com.ringo.mock.model.UserMock;
import com.ringo.model.company.Ticket;
import com.ringo.model.security.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private AuthenticationProperties config;

    @Test
    void accessTokenSuccess() {
        User user = UserMock.getUserMock();

        when(config.getAccessTokenExpirationMillis()).thenReturn(10000L);
        when(config.getSecret()).thenReturn("secret");
        when(config.isUsePasswordHash()).thenReturn(true);

        String accessToken = jwtService.generateAccessToken(user);

        assertOnlyValidForType(user, accessToken, TokenType.ACCESS);
    }

    @Test
    void refreshTokenSuccess() {
        User user = UserMock.getUserMock();

        when(config.getRefreshTokenExpirationMillis()).thenReturn(10000L);
        when(config.getSecret()).thenReturn("secret");
        when(config.isUsePasswordHash()).thenReturn(true);

        String refreshToken = jwtService.generateRefreshToken(user);

        assertOnlyValidForType(user, refreshToken, TokenType.REFRESH);
    }

    @Test
    void recoverPasswordTokenSuccess() {
        User user = UserMock.getUserMock();

        when(config.getRecoverPasswordTokenExpirationMillis()).thenReturn(10000L);
        when(config.getSecret()).thenReturn("secret");
        when(config.isUsePasswordHash()).thenReturn(true);

        String recoverPasswordToken = jwtService.generateRecoverPasswordToken(user);

        assertOnlyValidForType(user, recoverPasswordToken, TokenType.RECOVER);
    }

    @Test
    void emailVerificationTokenSuccess() {
        User user = UserMock.getUserMock();

        when(config.getEmailVerificationTokenExpirationMillis()).thenReturn(10000L);
        when(config.getSecret()).thenReturn("secret");
        when(config.isUsePasswordHash()).thenReturn(true);

        String emailVerificationToken = jwtService.generateEmailVerificationToken(user);

        assertOnlyValidForType(user, emailVerificationToken, TokenType.EMAIL_VERIFICATION);
    }

    @Test
    void ticketTokenSuccess() {
        Ticket ticket = TicketMock.getTicketMock();

        when(config.getSecret()).thenReturn("secret");
        when(config.isUsePasswordHash()).thenReturn(true);

        String ticketToken = jwtService.generateTicketCode(ticket);

        assertOnlyValidForType(ticket.getId().getParticipant(), ticketToken, null);

        assertDoesNotThrow(() -> jwtService.verifyTicketCode(ticketToken));
    }

    private void assertOnlyValidForType(User user, String token, TokenType type) {
        if(type != null)
            assertTrue(jwtService.isTokenValid(user, token, type));

        for(TokenType other : TokenType.values()) {
            if(other != type) {
                assertFalse(jwtService.isTokenValid(user, token, other));
            }
        }
    }
}
