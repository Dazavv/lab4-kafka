package com.hs.lab4.userservice.controller;

import com.hs.lab4.userservice.UserServiceApplication;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(controllers = AuthController.class)
@ContextConfiguration(classes = UserServiceApplication.class)
@Import(AuthControllerTestSecurityConfig.class)
class AuthControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserMapper userMapper;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private RefreshJwtRequest refreshJwtRequest;
    private AddRoleToUserRequest addRoleRequest;
    private User testUser;
    private UserDto testUserDto;
    private JwtResponse jwtResponse;

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

        webTestClient
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-token")
                .jsonPath("$.refreshToken").isEqualTo("refresh-token");
    }

//    @Test
//    void testRegister_Success() {
//        User newUser = new User();
//        newUser.setId(2L);
//        newUser.setLogin("newuser");
//        newUser.setName("New");
//        newUser.setSurname("User");
//        newUser.setEmail("newuser@example.com");
//        newUser.setRoles(Set.of(Role.USER));
//
//        UserDto newUserDto = new UserDto(2L, "newuser", "New", "User", "newuser@example.com", Set.of(Role.USER));
//
//        when(authService.register(
//                "newuser", "password", "newuser@example.com",
//                "New", "User", Role.USER
//        )).thenReturn(Mono.just(newUser));
//        when(userMapper.toUserDto(newUser)).thenReturn(newUserDto);
//
//        webTestClient
//                .mutateWith(csrf())
//                .post()
//                .uri("/api/v1/auth/register")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(registerRequest)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody()
//                .jsonPath("$.id").isEqualTo(2)
//                .jsonPath("$.login").isEqualTo("newuser");
//    }

    @Test
    void testRegister_WithServiceRole_ShouldReturnError() {
        registerRequest.setRole(Role.SERVICE);

        webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isUnauthorized();

        verify(authService, never()).register(anyString(), anyString(), anyString(),
                anyString(), anyString(), any(Role.class));
    }

    @Test
    void testGetNewAccessToken_Success() {
        JwtResponse response = new JwtResponse("new-access-token", null);
        when(authService.getAccessToken("refresh-token")).thenReturn(Mono.just(response));

        webTestClient
                .post()
                .uri("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshJwtRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("new-access-token");
    }

    @Test
    void testGetNewRefreshToken_Success() {
        JwtResponse response = new JwtResponse("new-access-token", "new-refresh-token");
        when(authService.refresh("refresh-token")).thenReturn(Mono.just(response));

        webTestClient
                .post()
                .uri("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshJwtRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("new-access-token")
                .jsonPath("$.refreshToken").isEqualTo("new-refresh-token");
    }

    @Test
    void testLogout_Success() {
        JwtResponse response = new JwtResponse(null, null);
        when(authService.logout("refresh-token")).thenReturn(Mono.just(response));

        webTestClient
                .post()
                .uri("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(refreshJwtRequest)
                .exchange()
                .expectStatus().isOk();
    }
}
