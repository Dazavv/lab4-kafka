package com.hs.lab4.userservice.service;//package com.hs.lab3.userservice.service;
//
//import com.hs.lab3.userservice.dto.responses.UserDto;
//import com.hs.lab3.userservice.entity.User;
//import com.hs.lab3.userservice.enums.Role;
//import com.hs.lab3.userservice.jwt.JwtProvider;
//import com.hs.lab3.userservice.repository.UserRepository;
//import com.hs.lab3.userservice.dto.requests.CreateUserRequest;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.containers.wait.strategy.Wait;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//
//import java.util.Set;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
//@Testcontainers
//@ActiveProfiles("test")
//class UserServiceIntegrationTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private JwtProvider jwtProvider;
//
//    private CreateUserRequest createRequest;
//    private String accessToken;
//
//    @BeforeEach
//    void setUp() {
//        createRequest = new CreateUserRequest("integrationuser", "Integration", "Test");
//
//        userRepository.deleteAll();
//
//        // Генерируем тестового пользователя и токен
//        var testUser = userRepository.saveAndFlush(new User(
//                null,
//                "integrationuser",
//                "password",
//                "Integration",
//                "Test",
//                "integration@test.com",
//                Set.of(Role.ADMIN)
//        ));
//
//        accessToken = jwtProvider.generateAccessToken(testUser);
//    }
//
//    @Test
//    void testCreateAndGetUser() {
//        // добавляем заголовок Authorization
//        UserDto createdUser = webTestClient.post()
//                .uri("/api/v1/user")
//                .header("Authorization", "Bearer " + accessToken)
//                .bodyValue(createRequest)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody(UserDto.class)
//                .returnResult()
//                .getResponseBody();
//
//        Assertions.assertThat(createdUser).isNotNull();
//        Assertions.assertThat(createdUser.login()).isEqualTo("integrationuser");
//
//        webTestClient.get()
//                .uri("/api/v1/user/{id}", createdUser.id())
//                .header("Authorization", "Bearer " + accessToken)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserDto.class)
//                .value(user -> Assertions.assertThat(user.login()).isEqualTo("integrationuser"));
//    }
//
//    @Test
//    void testGetAllUsers() {
//        for (int i = 0; i < 3; i++) {
//            CreateUserRequest request = new CreateUserRequest("user" + i, "Name" + i, "Surname" + i);
//            webTestClient.post()
//                    .uri("/api/v1/user")
//                    .header("Authorization", "Bearer " + accessToken)
//                    .bodyValue(request)
//                    .exchange()
//                    .expectStatus().isCreated();
//        }
//
//        webTestClient.get()
//                .uri("/api/v1/user?page=0&size=10")
//                .header("Authorization", "Bearer " + accessToken)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.content.length()").value(length -> Assertions.assertThat((Integer) length).isGreaterThanOrEqualTo(3));
//    }
//
//    @Test
//    void testDeleteUser() {
//        UserDto createdUser = webTestClient.post()
//                .uri("/api/v1/user")
//                .header("Authorization", "Bearer " + accessToken)
//                .bodyValue(createRequest)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody(UserDto.class)
//                .returnResult()
//                .getResponseBody();
//
//        webTestClient.delete()
//                .uri("/api/v1/user/id/{id}", createdUser.id())
//                .header("Authorization", "Bearer " + accessToken)
//                .exchange()
//                .expectStatus().isOk();
//
//        webTestClient.get()
//                .uri("/api/v1/user/{id}", createdUser.id())
//                .header("Authorization", "Bearer " + accessToken)
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//}
