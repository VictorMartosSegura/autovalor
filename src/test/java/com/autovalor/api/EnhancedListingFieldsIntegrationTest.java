package com.autovalor.api;

import static org.hamcrest.Matchers.hasSize;
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
        "spring.datasource.url=jdbc:h2:mem:enhanced_listing_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-enhanced-listing-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin-enhanced@autovalor.test",
        "app.admin.password=AdminEnhanced123"
})
class EnhancedListingFieldsIntegrationTest {

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
    void shouldCreateListingWithEnhancedVehicleFields() throws Exception {
        String token = registerUserAndExtractToken();

        mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(enhancedListingJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.location").value("Valencia"))
                .andExpect(jsonPath("$.province").value("Valencia"))
                .andExpect(jsonPath("$.sellerType").value("Particular"))
                .andExpect(jsonPath("$.bodyType").value("Compacto"))
                .andExpect(jsonPath("$.doors").value(5))
                .andExpect(jsonPath("$.powerCv").value(150))
                .andExpect(jsonPath("$.engineSize").value("2.0"))
                .andExpect(jsonPath("$.environmentalLabel").value("C"))
                .andExpect(jsonPath("$.warranty").value(true))
                .andExpect(jsonPath("$.color").value("Gris"))
                .andExpect(jsonPath("$.registrationMonth").value(6))
                .andExpect(jsonPath("$.registrationYear").value(2018))
                .andExpect(jsonPath("$.previousOwners").value(1))
                .andExpect(jsonPath("$.financeable").value(true))
                .andExpect(jsonPath("$.maintenanceBook").value(true));
    }

    @Test
    void shouldFilterByEnhancedFields() throws Exception {
        String token = registerUserAndExtractToken();
        createEnhancedListing(token);

        mockMvc.perform(get("/api/cars")
                        .param("province", "Valencia")
                        .param("sellerType", "Particular")
                        .param("bodyType", "Compacto")
                        .param("doors", "5")
                        .param("minPowerCv", "140")
                        .param("maxPowerCv", "160")
                        .param("environmentalLabel", "C")
                        .param("warranty", "true")
                        .param("financeable", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Volkswagen Golf 2018"));
    }

    private String registerUserAndExtractToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Enhanced Seller",
                                  "email": "enhanced-seller@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return result.getResponse().getContentAsString()
                .replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    private void createEnhancedListing(String token) throws Exception {
        mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(enhancedListingJson()))
                .andExpect(status().isCreated());
    }

    private String enhancedListingJson() {
        return """
                {
                  "title": "Volkswagen Golf 2018",
                  "description": "Coche en muy buen estado con mantenimiento al dia",
                  "price": 12500,
                  "brand": "Volkswagen",
                  "model": "Golf",
                  "year": 2018,
                  "km": 105000,
                  "fuelType": "Diesel",
                  "transmission": "Manual",
                  "location": "Valencia",
                  "province": "Valencia",
                  "sellerType": "Particular",
                  "bodyType": "Compacto",
                  "doors": 5,
                  "powerCv": 150,
                  "engineSize": "2.0",
                  "environmentalLabel": "C",
                  "warranty": true,
                  "color": "Gris",
                  "registrationMonth": 6,
                  "registrationYear": 2018,
                  "previousOwners": 1,
                  "financeable": true,
                  "maintenanceBook": true
                }
                """;
    }
}
