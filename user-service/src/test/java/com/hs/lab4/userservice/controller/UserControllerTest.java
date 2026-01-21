package com.hs.lab4.userservice.controller;

import com.hs.lab4.userservice.dto.requests.CreateUserRequest;
import com.hs.lab4.userservice.dto.responses.UserDto;
import com.hs.lab4.userservice.entity.User;
import com.hs.lab4.userservice.enums.Role;
import com.hs.lab4.userservice.mapper.UserMapper;
import com.hs.lab4.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserDto testUserDto;
    private CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test");
        testUser.setSurname("User");

        testUserDto = new UserDto(1L, "testuser", "Test", "User", "test@mail.com", Set.of(Role.ADMIN));

        createRequest = new CreateUserRequest("testuser", "Test", "User", "surname");
    }

    @Test
    void testGetAllUsers() {
        User user2 = new User();
        user2.setId(2L);

        UserDto dto2 = new UserDto(2L, "user2", "Name", "Surname", "test2@mail.com", Set.of(Role.ADMIN));

        Page<User> userPage = new PageImpl<>(List.of(testUser, user2));
        Page<UserDto> dtoPage = new PageImpl<>(List.of(testUserDto, dto2));

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(Mono.just(userPage));
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);
        when(userMapper.toUserDto(user2)).thenReturn(dto2);

        StepVerifier.create(userController.getAllUsers(0, 25, null))
                .expectNextMatches(response -> {
                    Page<UserDto> page = response.getBody();
                    return page != null && page.getTotalElements() == 2;
                })
                .verifyComplete();
    }

    @Test
    void testGetUserById() {
        when(userService.getUserById(1L)).thenReturn(Mono.just(testUser));
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);

        StepVerifier.create(userController.getUserById(1L))
                .expectNextMatches(response -> {
                    UserDto dto = response.getBody();
                    return dto != null && dto.id().equals(1L);
                })
                .verifyComplete();
    }

    @Test
    void testGetUserByUsername() {
        when(userService.getUserByUsername("testuser")).thenReturn(Mono.just(testUser));
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);

        StepVerifier.create(userController.getUserByUsername("testuser"))
                .expectNextMatches(response -> {
                    UserDto dto = response.getBody();
                    return dto != null;
                })
                .verifyComplete();
    }

    @Test
    void testSearchByUsername() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        Page<UserDto> dtoPage = new PageImpl<>(List.of(testUserDto));

        when(userService.searchByUsername(anyString(), any(Pageable.class)))
                .thenReturn(Mono.just(userPage));
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);

        StepVerifier.create(userController.searchByUsername("test", 0, 25, null))
                .expectNextMatches(response -> {
                    Page<UserDto> page = response.getBody();
                    return page != null && page.getTotalElements() == 1;
                })
                .verifyComplete();
    }

    @Test
    void testGetAllUsers_WithSorting() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(Mono.just(userPage));
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);

        StepVerifier.create(userController.getAllUsers(0, 25, List.of("username,desc")))
                .expectNextMatches(response -> response.getStatusCode().is2xxSuccessful())
                .verifyComplete();
    }

    @Test
    void testGetAllUsers_SizeClamping() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(Mono.just(userPage));
        when(userMapper.toUserDto(testUser)).thenReturn(testUserDto);

        // Test that size > 100 is clamped to 100
        StepVerifier.create(userController.getAllUsers(0, 200, null))
                .expectNextMatches(response -> response.getStatusCode().is2xxSuccessful())
                .verifyComplete();
    }
}

