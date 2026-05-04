package com.autovalor.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autovalor.api.repository.ContactMessageRepository;
import com.autovalor.api.repository.FavoriteRepository;
import com.autovalor.api.repository.ListingImageRepository;
import com.autovalor.api.repository.ListingRepository;
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
        "spring.datasource.url=jdbc:h2:mem:error_handling_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-error-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin-error@autovalor.test",
        "app.admin.password=AdminError123"
})
class ErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ListingImageRepository listingImageRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        contactMessageRepository.deleteAll();
        favoriteRepository.deleteAll();
        listingImageRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnStandardValidationError() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "invalid-email",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Error de validacion"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void shouldReturnStandardNotFoundError() throws Exception {
        mockMvc.perform(get("/api/cars/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Anuncio no encontrado"))
                .andExpect(jsonPath("$.path").value("/api/cars/999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnStandardTypeMismatchError() throws Exception {
        mockMvc.perform(get("/api/cars")
                        .param("minPrice", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Parametro no valido"))
                .andExpect(jsonPath("$.fieldErrors.minPrice").value("Valor no valido"));
    }

    @Test
    void shouldReturnStandardUnauthorizedErrorForBadCredentials() throws Exception {
        registerUser("error-user@test.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "error-user@test.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void shouldReturnStandardForbiddenError() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-error@test.com");
        Long listingId = createListingAndExtractId(sellerToken);
        String otherToken = registerUserAndExtractToken("other-error@test.com");

        mockMvc.perform(get("/api/cars/{listingId}/contact-messages", listingId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("No puedes ver los mensajes de este anuncio"));
    }

    private void registerUser(String email) throws Exception {
        registerUserAndExtractToken(email);
    }

    private String registerUserAndExtractToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Error User",
                                  "email": "%s",
                                  "password": "password123"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();

        return result.getResponse().getContentAsString()
                .replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    private Long createListingAndExtractId(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Volkswagen Golf 2018",
                                  "description": "Coche en buen estado",
                                  "price": 12500,
                                  "brand": "Volkswagen",
                                  "model": "Golf",
                                  "year": 2018,
                                  "km": 105000,
                                  "fuelType": "Diesel",
                                  "transmission": "Manual"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return Long.valueOf(result.getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":([0-9]+).*", "$1"));
    }
}
