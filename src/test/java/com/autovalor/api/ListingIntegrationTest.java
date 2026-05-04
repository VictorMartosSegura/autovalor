package com.autovalor.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        "spring.datasource.url=jdbc:h2:mem:listing_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-listing-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin-listing@autovalor.test",
        "app.admin.password=AdminListing123"
})
class ListingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateListingWithAuthenticatedUser() throws Exception {
        String token = registerUserAndExtractToken("seller@test.com");

        mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(defaultListingJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value("Volkswagen Golf 2018"))
                .andExpect(jsonPath("$.brand").value("Volkswagen"))
                .andExpect(jsonPath("$.model").value("Golf"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.userName").value("Seller"));
    }

    @Test
    void shouldListPublicListings() throws Exception {
        String token = registerUserAndExtractToken("seller@test.com");
        createListingAndExtractId(token);

        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Volkswagen Golf 2018"));
    }

    @Test
    void shouldGetListingById() throws Exception {
        String token = registerUserAndExtractToken("seller@test.com");
        Long listingId = createListingAndExtractId(token);

        mockMvc.perform(get("/api/cars/{id}", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(listingId))
                .andExpect(jsonPath("$.brand").value("Volkswagen"));
    }

    @Test
    void shouldUpdateOwnListing() throws Exception {
        String token = registerUserAndExtractToken("seller@test.com");
        Long listingId = createListingAndExtractId(token);

        mockMvc.perform(put("/api/cars/{id}", listingId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Volkswagen Golf actualizado",
                                  "description": "Descripcion actualizada",
                                  "price": 14000,
                                  "brand": "Volkswagen",
                                  "model": "Golf",
                                  "year": 2019,
                                  "km": 90000,
                                  "fuelType": "Gasolina",
                                  "transmission": "Automatico"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Volkswagen Golf actualizado"))
                .andExpect(jsonPath("$.price").value(14000))
                .andExpect(jsonPath("$.year").value(2019));
    }

    @Test
    void shouldUpdateListingStatus() throws Exception {
        String token = registerUserAndExtractToken("seller@test.com");
        Long listingId = createListingAndExtractId(token);

        mockMvc.perform(patch("/api/cars/{id}/status", listingId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RESERVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    void shouldDeleteOwnListing() throws Exception {
        String token = registerUserAndExtractToken("seller@test.com");
        Long listingId = createListingAndExtractId(token);

        mockMvc.perform(delete("/api/cars/{id}", listingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cars/{id}", listingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectCreateListingWithoutToken() throws Exception {
        mockMvc.perform(post("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(defaultListingJson()))
                .andExpect(status().isUnauthorized());
    }

    private String registerUserAndExtractToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Seller",
                                  "email": "%s",
                                  "password": "password123"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return response.replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    private Long createListingAndExtractId(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(defaultListingJson()))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String id = response.replaceAll(".*\\\"id\\\":([0-9]+).*", "$1");
        return Long.valueOf(id);
    }

    private String defaultListingJson() {
        return """
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
                """;
    }
}
