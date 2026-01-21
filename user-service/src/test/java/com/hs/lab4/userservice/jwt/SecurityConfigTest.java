package com.hs.lab4.userservice.jwt;//package com.hs.lab3.userservice.jwt;
//
//import com.hs.lab3.userservice.config.SecurityConfig;
//import com.hs.lab3.userservice.jwt.JwtProvider;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class SecurityConfigEdgeCasesTest {
//
//    @Mock
//    private JwtProvider jwtProvider;
//
//    @Test
//    void shouldHandleNullJwtProvider() {
//        // Этот тест проверяет, что конфигурация не падает при создании
//        // В реальности Spring не позволит создать bean с null зависимостью
//        SecurityConfig securityConfig = new SecurityConfig(jwtProvider);
//        assert securityConfig != null;
//    }
//
//    @Test
//    void shouldBuildFilterChainWithMinimalConfiguration() {
//        // Arrange
//        SecurityConfig securityConfig = new SecurityConfig(jwtProvider);
//        ServerHttpSecurity http = mock(ServerHttpSecurity.class);
//
//        ServerHttpSecurity httpAfterCsrf = mock(ServerHttpSecurity.class);
//
//        when(http.csrf(any())).thenReturn(httpAfterCsrf);
//
//        ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchangeSpec =
//                mock(ServerHttpSecurity.AuthorizeExchangeSpec.class);
//        when(httpAfterCsrf.authorizeExchange()).thenReturn(authorizeExchangeSpec);
//
//        ServerHttpSecurity.AuthorizeExchangeSpec.PathMatchersAuthorizeExchangeSpec pathMatchersSpec =
//                mock(ServerHttpSecurity.AuthorizeExchangeSpec.PathMatchersAuthorizeExchangeSpec.class);
//        when(authorizeExchangeSpec.pathMatchers(any(String[].class))).thenReturn(pathMatchersSpec);
//        when(pathMatchersSpec.permitAll()).thenReturn(authorizeExchangeSpec);
//
//        ServerHttpSecurity.AuthorizeExchangeSpec.AccessExchangeSpec accessExchangeSpec =
//                mock(ServerHttpSecurity.AuthorizeExchangeSpec.AccessExchangeSpec.class);
//        when(authorizeExchangeSpec.anyExchange()).thenReturn(accessExchangeSpec);
//        when(accessExchangeSpec.authenticated()).thenReturn(httpAfterCsrf);
//
//        when(httpAfterCsrf.addFilterAt(any(), any())).thenReturn(httpAfterCsrf);
//        when(httpAfterCsrf.build()).thenReturn(mock(SecurityWebFilterChain.class));
//
//        // Act
//        SecurityWebFilterChain chain = securityConfig.securityWebFilterChain(http);
//
//        // Assert
//        assertNotNull(chain);
//        verify(http).csrf(any());
//    }
//}