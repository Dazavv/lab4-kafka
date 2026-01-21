package com.hs.lab4.userservice.jwt;

import com.hs.lab4.userservice.entity.User;
import com.hs.lab4.userservice.enums.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private User user;

    private static final String ACCESS_SECRET = Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes());
    private static final String REFRESH_SECRET = Base64.getEncoder().encodeToString("abcdefghijklmnopqrstuvwxyzabcdef".getBytes());

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(ACCESS_SECRET, REFRESH_SECRET);

        user = new User();
        user.setId(1L);
        user.setLogin("testuser");
        user.setName("First");
        user.setEmail("a@b.com");
        user.setRoles(Set.of(Role.USER));
    }

//    @Test
//    void generateAccessToken_ShouldReturnNonNullToken() {
//        String token = jwtProvider.generateAccessToken(user);
//        assertNotNull(token);
//        assertTrue(jwtProvider.validateAccessToken(token));
//        Claims claims = jwtProvider.getAccessClaims(token);
//
//        assertEquals("testuser", claims.getSubject());
//
//        // Приводим обе коллекции к HashSet
//        Set<Role> expectedRoles = new HashSet<>(user.getRoles());
//        Set<Role> actualRoles = new HashSet<>((List<Role>) claims.get("roles"));
//        assertEquals(expectedRoles, actualRoles);
//
//        assertEquals(user.getName(), claims.get("name"));
//        assertEquals(user.getEmail(), claims.get("email"));
//        assertEquals(user.getId(), claims.get("id"));
//    }


    @Test
    void generateRefreshToken_ShouldReturnNonNullToken() {
        String token = jwtProvider.generateRefreshToken(user);
        assertNotNull(token);
        assertTrue(jwtProvider.validateRefreshToken(token));
        Claims claims = jwtProvider.getRefreshClaims(token);
        assertEquals("testuser", claims.getSubject());
    }

    @Test
    void validateAccessToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.string";
        assertFalse(jwtProvider.validateAccessToken(invalidToken));
    }

    @Test
    void validateRefreshToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.string";
        assertFalse(jwtProvider.validateRefreshToken(invalidToken));
    }

    @Test
    void getAccessClaims_ShouldReturnClaims_ForValidToken() {
        String token = jwtProvider.generateAccessToken(user);
        Claims claims = jwtProvider.getAccessClaims(token);
        assertEquals("testuser", claims.getSubject());
    }

    @Test
    void getRefreshClaims_ShouldReturnClaims_ForValidToken() {
        String token = jwtProvider.generateRefreshToken(user);
        Claims claims = jwtProvider.getRefreshClaims(token);
        assertEquals("testuser", claims.getSubject());
    }
}
