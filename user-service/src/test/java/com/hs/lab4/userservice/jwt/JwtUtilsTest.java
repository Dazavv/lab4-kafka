package com.hs.lab4.userservice.jwt;

import com.hs.lab4.userservice.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private Claims createClaims(String subject, String email, Object id, List<String> roles) {
        return Jwts.claims()
                .subject(subject)
                .add("email", email)
                .add("id", id)
                .add("roles", roles)
                .build();
    }

    @Test
    void generate_shouldCorrectlyParseClaims_withIntegerId() {
        Claims claims = createClaims(
                "testUser",
                "test@example.com",
                123,
                List.of("ADMIN", "USER")
        );

        JwtAuthentication auth = JwtUtils.generate(claims);

        assertThat(auth.getLogin()).isEqualTo("testUser");
        assertThat(auth.getEmail()).isEqualTo("test@example.com");
        assertThat(auth.getId()).isEqualTo(123L);
        assertThat(auth.getRoles())
                .containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
    }

    @Test
    void generate_shouldCorrectlyParseClaims_withLongId() {
        Claims claims = createClaims(
                "anotherUser",
                "another@example.com",
                987L,
                List.of("USER")
        );

        JwtAuthentication auth = JwtUtils.generate(claims);

        assertThat(auth.getLogin()).isEqualTo("anotherUser");
        assertThat(auth.getEmail()).isEqualTo("another@example.com");
        assertThat(auth.getId()).isEqualTo(987L);
        assertThat(auth.getRoles()).containsExactly(Role.USER);
    }

    @Test
    void generate_shouldHandleSingleRoleCorrectly() {
        Claims claims = createClaims(
                "oneRole",
                "one@example.com",
                55L,
                List.of("REDACTOR")
        );

        JwtAuthentication auth = JwtUtils.generate(claims);

        assertThat(auth.getRoles()).containsExactly(Role.REDACTOR);
    }

    @Test
    void generate_shouldHandleEmptyRolesList() {
        Claims claims = createClaims(
                "emptyRoles",
                "empty@example.com",
                1L,
                List.of()
        );

        JwtAuthentication auth = JwtUtils.generate(claims);

        assertThat(auth.getRoles()).isEmpty();
    }
}
