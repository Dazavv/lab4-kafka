package com.hs.lab4.userservice.service;

import com.hs.lab4.userservice.entity.User;
import com.hs.lab4.userservice.enums.Role;
import com.hs.lab4.userservice.exceptions.UserNotFoundException;
import com.hs.lab4.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;

    @BeforeEach
    void setUp() {
        user1 = new User(
                1L,
                "testuser",
                "hashedPwd",
                "Test",
                "User",
                "test@mail.com",
                Set.of(Role.USER)
        );
    }

    @Test
    void testGetAllUsers() {
        User user2 = new User(
                2L,
                "admin",
                "hashedPwd2",
                "Admin",
                "Boss",
                "admin@mail.com",
                Set.of(Role.ADMIN)
        );

        Pageable pageable = PageRequest.of(0, 25);
        Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        StepVerifier.create(userService.getAllUsers(pageable))
                .expectNextMatches(page -> page.getTotalElements() == 2 && page.getContent().size() == 2)
                .verifyComplete();

        verify(userRepository).findAll(eq(pageable));
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        StepVerifier.create(userService.getUserById(1L))
                .expectNextMatches(u -> u.getId().equals(1L) && u.getLogin().equals("testuser"))
                .verifyComplete();

        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        StepVerifier.create(userService.getUserById(1L))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserByUsername_Success() {
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(user1));

        StepVerifier.create(userService.getUserByUsername("testuser"))
                .expectNextMatches(u -> u.getLogin().equals("testuser") && u.getEmail().equals("test@mail.com"))
                .verifyComplete();

        verify(userRepository).findByLogin("testuser");
    }

    @Test
    void testGetUserByUsername_NotFound() {
        when(userRepository.findByLogin("testuser")).thenReturn(Optional.empty());

        StepVerifier.create(userService.getUserByUsername("testuser"))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(userRepository).findByLogin("testuser");
    }

    @Test
    void testDeleteUserById_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        StepVerifier.create(userService.deleteUserById(1L))
                .verifyComplete();

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUserById_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        StepVerifier.create(userService.deleteUserById(1L))
                .expectError(UserNotFoundException.class)
                .verify();

        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testSearchByUsername() {
        Pageable pageable = PageRequest.of(0, 25);
        Page<User> userPage = new PageImpl<>(List.of(user1), pageable, 1);

        when(userRepository.findByLoginContainingIgnoreCase(eq("test"), eq(pageable)))
                .thenReturn(userPage);

        StepVerifier.create(userService.searchByUsername("test", pageable))
                .expectNextMatches(page -> page.getTotalElements() == 1
                        && page.getContent().get(0).getLogin().equals("testuser"))
                .verifyComplete();

        verify(userRepository).findByLoginContainingIgnoreCase("test", pageable);
    }
}
