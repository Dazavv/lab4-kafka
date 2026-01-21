package com.hs.lab4.userservice.jwt;

import com.hs.lab4.userservice.entity.User;
import com.hs.lab4.userservice.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.mockito.Mockito.*;

class JwtFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private WebFilterChain chain;

    @Mock
    private ServerWebExchange.Builder exchangeBuilder;

    private JwtFilter jwtFilter;

    private static final String SERVICE_TOKEN = "service-token";
    private static final String ACCESS_TOKEN = "access-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtFilter = new JwtFilter(jwtProvider);
        // Устанавливаем serviceToken через Reflection
        java.lang.reflect.Field field;
        try {
            field = JwtFilter.class.getDeclaredField("serviceToken");
            field.setAccessible(true);
            field.set(jwtFilter, SERVICE_TOKEN);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_ShouldContinueChain_WhenNoAuthorizationHeader() {
        when(exchange.getRequest()).thenReturn(mock(org.springframework.http.server.reactive.ServerHttpRequest.class));
        when(exchange.getRequest().getHeaders()).thenReturn(HttpHeaders.EMPTY);

        Mono<Void> result = jwtFilter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtProvider);
    }

    @Test
    void filter_ShouldAuthenticateServiceToken() {
        var request = mock(org.springframework.http.server.reactive.ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + SERVICE_TOKEN);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        Mono<Void> result = jwtFilter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtProvider);
    }

    @Test
    void filter_ShouldContinueChain_WhenTokenInvalid() {
        var request = mock(org.springframework.http.server.reactive.ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);
        when(jwtProvider.validateAccessToken(ACCESS_TOKEN)).thenReturn(false);

        Mono<Void> result = jwtFilter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_ShouldAuthenticate_WhenTokenValid() {
        User user = new User(
                1L,
                "testuser",
                "password",
                "First",
                "Last",
                "a@b.com",
                Set.of(Role.USER)
        );

        String token = jwtProvider.generateAccessToken(user);

        var request = mock(org.springframework.http.server.reactive.ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(headers);

        Mono<Void> result = jwtFilter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }

}
