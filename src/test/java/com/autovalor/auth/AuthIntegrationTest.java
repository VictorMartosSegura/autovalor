package com.autovalor.auth;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autovalor.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:autovalor_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin@autovalor.test",
        "app.admin.password=AdminTest123"
})
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserWithUserRole() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Victor User",
                                  "email": "victoruser@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.name").value("Victor User"))
                .andExpect(jsonPath("$.email").value("victoruser@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldLoginRegisteredUser() throws Exception {
        registerDefaultUser();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "victoruser@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldReturnCurrentUserWithValidToken() throws Exception {
        String token = registerDefaultUserAndExtractToken();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Victor User"))
                .andExpect(jsonPath("$.email").value("victoruser@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void shouldRejectCurrentUserWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLoginAdminUser() throws Exception {
        createAdminByRestartingInitializerLogic();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@autovalor.test",
                                  "password": "AdminTest123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldRejectDuplicatedEmail() throws Exception {
        registerDefaultUser();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Other User",
                                  "email": "victoruser@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ya existe una cuenta con este email"));
    }

    private void registerDefaultUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Victor User",
                                  "email": "victoruser@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    private String registerDefaultUserAndExtractToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Victor User",
                                  "email": "victoruser@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return response.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    private void createAdminByRestartingInitializerLogic() {
        // The test clears the database before each test, so the application startup admin is also removed.
        // Create an admin through the repository using the same user model, then log in through the public endpoint.
        com.autovalor.user.User admin = new com.autovalor.user.User(
                "AutoValor Admin",
                "admin@autovalor.test",
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("AdminTest123"),
                com.autovalor.user.Role.ADMIN
        );
        userRepository.save(admin);
    }
}
