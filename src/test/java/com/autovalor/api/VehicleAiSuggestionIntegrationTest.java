package com.autovalor.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autovalor.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:vehicle_ai_suggestion_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-ai-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin-ai@autovalor.test",
        "app.admin.password=AdminAi123",
        "app.ai.enabled=false"
})
class VehicleAiSuggestionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRejectAiSuggestionWithoutToken() throws Exception {
        mockMvc.perform(multipart("/api/ai/vehicle-suggestions")
                        .file(testImage())
                        .param("prompt", "Rellena los datos del coche"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnDisabledAiSuggestionWhenAiIsNotConfigured() throws Exception {
        String token = registerUserAndExtractToken();

        mockMvc.perform(multipart("/api/ai/vehicle-suggestions")
                        .file(testImage())
                        .param("prompt", "Es un coche gris compacto")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confidence").value(0.0))
                .andExpect(jsonPath("$.warnings[0]", containsString("La IA no esta activada")));
    }

    @Test
    void shouldRejectAiSuggestionWithoutImages() throws Exception {
        String token = registerUserAndExtractToken();

        mockMvc.perform(multipart("/api/ai/vehicle-suggestions")
                        .param("prompt", "Rellena los datos del coche")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUnsupportedImageFormatForAiSuggestion() throws Exception {
        String token = registerUserAndExtractToken();
        MockMultipartFile file = new MockMultipartFile(
                "images",
                "file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not an image".getBytes()
        );

        mockMvc.perform(multipart("/api/ai/vehicle-suggestions")
                        .file(file)
                        .param("prompt", "Rellena los datos del coche")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    private MockMultipartFile testImage() {
        return new MockMultipartFile(
                "images",
                "car.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3, 4}
        );
    }

    private String registerUserAndExtractToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "AI User",
                                  "email": "ai-user@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return result.getResponse().getContentAsString()
                .replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }
}
