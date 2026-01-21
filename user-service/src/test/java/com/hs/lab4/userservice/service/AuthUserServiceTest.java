package com.hs.lab4.userservice.service;

import com.hs.lab4.userservice.entity.User;
import com.hs.lab4.userservice.enums.Role;
import com.hs.lab4.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthUserService authUserService;

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


    @Test
    void getByLogin_ShouldReturnUser_WhenExists() {
        when(userRepository.findByLogin("user")).thenReturn(Optional.of(user));

        Optional<User> result = authUserService.getByLogin("user");

        assertTrue(result.isPresent());
        assertEquals("user", result.get().getLogin());
    }

    @Test
    void getByLogin_ShouldReturnEmpty_WhenNotExists() {
        when(userRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        Optional<User> result = authUserService.getByLogin("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void checkExistedUser_ShouldReturnTrue_WhenLoginExists() {
        when(userRepository.existsByLogin("user")).thenReturn(true);
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);

        boolean exists = authUserService.checkExistedUser("user", "a@b.com");

        assertTrue(exists);
    }

    @Test
    void checkExistedUser_ShouldReturnTrue_WhenEmailExists() {
        when(userRepository.existsByLogin("user")).thenReturn(false);
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        boolean exists = authUserService.checkExistedUser("user", "a@b.com");

        assertTrue(exists);
    }

    @Test
    void checkExistedUser_ShouldReturnFalse_WhenNeitherExists() {
        when(userRepository.existsByLogin("user")).thenReturn(false);
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);

        boolean exists = authUserService.checkExistedUser("user", "a@b.com");

        assertFalse(exists);
    }

    @Test
    void saveNewUser_ShouldReturnSavedUser() {
        when(userRepository.save(user)).thenReturn(user);

        User saved = authUserService.saveNewUser(user);

        assertNotNull(saved);
        assertEquals(user.getLogin(), saved.getLogin());
    }

    @Test
    void addNewRole_ShouldAddRoleAndSaveUser() {
        when(userRepository.save(user)).thenReturn(user);

        authUserService.addNewRole(user, Role.ADMIN);

        assertTrue(user.getRoles().contains(Role.ADMIN));
        verify(userRepository, times(1)).save(user);
    }

}
