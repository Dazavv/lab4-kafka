package com.hs.lab4.userservice.controller;

import com.hs.lab4.userservice.dto.requests.AddRoleToUserRequest;
import com.hs.lab4.userservice.dto.requests.LoginRequest;
import com.hs.lab4.userservice.dto.requests.RefreshJwtRequest;
import com.hs.lab4.userservice.dto.requests.RegisterRequest;
import com.hs.lab4.userservice.dto.responses.JwtResponse;
import com.hs.lab4.userservice.dto.responses.UserDto;
import com.hs.lab4.userservice.entity.User;
import com.hs.lab4.userservice.enums.Role;
import com.hs.lab4.userservice.mapper.UserMapper;
import com.hs.lab4.userservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private UserDto testUserDto;
    private JwtResponse jwtResponse;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private RefreshJwtRequest refreshJwtRequest;
    private AddRoleToUserRequest addRoleRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");
        testUser.setName("Test");
        testUser.setSurname("User");
        testUser.setEmail("test@example.com");
        testUser.setRoles(Set.of(Role.USER));

        testUserDto = new UserDto(1L, "testuser", "Test", "User", "test@example.com", Set.of(Role.USER));
        jwtResponse = new JwtResponse("access-token", "refresh-token");

        loginRequest = new LoginRequest();
        loginRequest.setLogin("testuser");
        loginRequest.setPassword("password");

        registerRequest = new RegisterRequest();
        registerRequest.setLogin("newuser");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setRole(Role.USER);

        refreshJwtRequest = new RefreshJwtRequest();
        refreshJwtRequest.setRefreshToken("refresh-token");

        addRoleRequest = new AddRoleToUserRequest();
        addRoleRequest.setLogin("testuser");
        addRoleRequest.setRole(Role.ADMIN);
    }

    @Test
    void testLogin_Success() {
        when(authService.login("testuser", "password")).thenReturn(Mono.just(jwtResponse));

        StepVerifier.create(authController.login(loginRequest))
                .expectNextMatches(response -> 
                    response.getAccessToken().equals("access-token") &&
                    response.getRefreshToken().equals("refresh-token")
                )
                .verifyComplete();

        verify(authService, times(1)).login("testuser", "password");
    }

    @Test
    void testRegister_Success() {
        when(authService.register(
                "newuser", "password", "newuser@example.com", 
                "New", "User", Role.USER
        )).thenReturn(Mono.just(testUser));
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);

        StepVerifier.create(authController.register(registerRequest))
                .expectNextMatches(response -> {
                    ResponseEntity<UserDto> entity = response;
                    return entity.getStatusCode() == HttpStatus.CREATED &&
                           entity.getBody() != null &&
                           entity.getBody().id().equals(1L);
                })
                .verifyComplete();

        verify(authService, times(1)).register(
                "newuser", "password", "newuser@example.com", 
                "New", "User", Role.USER
        );
        verify(userMapper, times(1)).toUserDto(testUser);
    }

    @Test
    void testRegister_ServiceRole_ShouldThrowAccessDeniedException() {
        registerRequest.setRole(Role.SERVICE);

        StepVerifier.create(authController.register(registerRequest))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(authService, never()).register(anyString(), anyString(), anyString(), 
                anyString(), anyString(), any(Role.class));
    }

    @Test
    void testGetNewAccessToken_Success() {
        JwtResponse response = new JwtResponse("new-access-token", null);
        when(authService.getAccessToken("refresh-token")).thenReturn(Mono.just(response));

        StepVerifier.create(authController.getNewAccessToken(refreshJwtRequest))
                .expectNextMatches(jwt -> 
                    jwt.getAccessToken().equals("new-access-token") &&
                    jwt.getRefreshToken() == null
                )
                .verifyComplete();

        verify(authService, times(1)).getAccessToken("refresh-token");
    }

    @Test
    void testGetNewRefreshToken_Success() {
        JwtResponse response = new JwtResponse("new-access-token", "new-refresh-token");
        when(authService.refresh("refresh-token")).thenReturn(Mono.just(response));

        StepVerifier.create(authController.getNewRefreshToken(refreshJwtRequest))
                .expectNextMatches(jwt -> 
                    jwt.getAccessToken().equals("new-access-token") &&
                    jwt.getRefreshToken().equals("new-refresh-token")
                )
                .verifyComplete();

        verify(authService, times(1)).refresh("refresh-token");
    }

    @Test
    void testLogout_Success() {
        JwtResponse response = new JwtResponse(null, null);
        when(authService.logout("refresh-token")).thenReturn(Mono.just(response));

        StepVerifier.create(authController.logout(refreshJwtRequest))
                .expectNextMatches(jwt -> 
                    jwt.getAccessToken() == null &&
                    jwt.getRefreshToken() == null
                )
                .verifyComplete();

        verify(authService, times(1)).logout("refresh-token");
    }

    @Test
    void testAddRoleToUser_Success() {
        when(authService.addRoleToUser("testuser", Role.ADMIN)).thenReturn(Mono.empty());

        StepVerifier.create(authController.addRoleToUser(addRoleRequest))
                .expectNextMatches(result -> 
                    result.equals("User with login: testuser has new role: ADMIN")
                )
                .verifyComplete();

        verify(authService, times(1)).addRoleToUser("testuser", Role.ADMIN);
    }
}
