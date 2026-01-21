package com.hs.lab4.userservice.service;

import com.hs.lab4.userservice.dto.responses.JwtResponse;
import com.hs.lab4.userservice.entity.User;
import com.hs.lab4.userservice.enums.Role;
import com.hs.lab4.userservice.exceptions.WrongCredentialsException;
import com.hs.lab4.userservice.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private AuthUserService authUserService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setLogin("user");
        user.setPassword("encodedPass");
        user.setName("First");
        user.setSurname("Last");
        user.setEmail("a@b.com");
        user.setRoles(new HashSet<>(Set.of(Role.USER)));
    }

    private void putRefreshTokenInStorage(String login, String token) throws Exception {
        java.lang.reflect.Field field = AuthService.class.getDeclaredField("refreshStorage");
        field.setAccessible(true);
        Map<String, String> storage = (Map<String, String>) field.get(authService);
        storage.put(login, token);
    }


    @Test
    void login_ShouldReturnJwtResponse_WhenPasswordMatches() {
        when(authUserService.getByLogin("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encodedPass")).thenReturn(true);
        when(jwtProvider.generateAccessToken(user)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(user)).thenReturn("refresh-token");

        Mono<JwtResponse> result = authService.login("user", "pass");

        StepVerifier.create(result)
                .expectNextMatches(jwt -> "access-token".equals(jwt.getAccessToken()) &&
                        "refresh-token".equals(jwt.getRefreshToken()))
                .verifyComplete();
    }

    @Test
    void login_ShouldError_WhenPasswordDoesNotMatch() {
        when(authUserService.getByLogin("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        Mono<JwtResponse> result = authService.login("user", "wrong");

        StepVerifier.create(result)
                .expectError(WrongCredentialsException.class)
                .verify();
    }

    @Test
    void getAccessToken_ShouldReturnNewAccessToken_WhenValidRefreshToken() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user");
        when(jwtProvider.validateRefreshToken("refresh-token")).thenReturn(true);
        when(jwtProvider.getRefreshClaims("refresh-token")).thenReturn(claims);
        when(authUserService.getByLogin("user")).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(user)).thenReturn("new-access");

        putRefreshTokenInStorage("user", "refresh-token");

        Mono<JwtResponse> result = authService.getAccessToken("refresh-token");

        StepVerifier.create(result)
                .expectNextMatches(jwt -> "new-access".equals(jwt.getAccessToken()))
                .verifyComplete();
    }

    @Test
    void refresh_ShouldReturnNewTokens_WhenValid() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user");
        when(jwtProvider.validateRefreshToken("refresh-token")).thenReturn(true);
        when(jwtProvider.getRefreshClaims("refresh-token")).thenReturn(claims);
        when(authUserService.getByLogin("user")).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(user)).thenReturn("new-access");
        when(jwtProvider.generateRefreshToken(user)).thenReturn("new-refresh");

        putRefreshTokenInStorage("user", "refresh-token");

        Mono<JwtResponse> result = authService.refresh("refresh-token");

        StepVerifier.create(result)
                .expectNextMatches(jwt -> "new-access".equals(jwt.getAccessToken()) &&
                        "new-refresh".equals(jwt.getRefreshToken()))
                .verifyComplete();
    }


    @Test
    void addRoleToUser_ShouldCallAuthUserService() {
        when(authUserService.getByLogin("user")).thenReturn(Optional.of(user));

        Mono<Void> result = authService.addRoleToUser("user", Role.ADMIN);

        StepVerifier.create(result).verifyComplete();
        verify(authUserService).addNewRole(user, Role.ADMIN);
    }
}
