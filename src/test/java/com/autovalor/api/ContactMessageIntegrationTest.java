package com.autovalor.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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
        "spring.datasource.url=jdbc:h2:mem:contact_message_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-contact-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin-contact@autovalor.test",
        "app.admin.password=AdminContact123"
})
class ContactMessageIntegrationTest {

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
    void shouldCreateContactMessageAsVisitor() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-contact@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);

        mockMvc.perform(post("/api/cars/{listingId}/contact", listingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactMessageJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.listingId").value(listingId))
                .andExpect(jsonPath("$.contactName").value("Buyer Visitor"))
                .andExpect(jsonPath("$.contactEmail").value("buyer@test.com"))
                .andExpect(jsonPath("$.message").value("Estoy interesado en este coche."));
    }

    @Test
    void shouldAllowListingOwnerToReadInbox() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-contact@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);
        createContactMessage(listingId);

        mockMvc.perform(get("/api/contact-messages")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].listingId").value(listingId))
                .andExpect(jsonPath("$[0].contactEmail").value("buyer@test.com"));
    }

    @Test
    void shouldAllowListingOwnerToReadListingMessages() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-contact@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);
        createContactMessage(listingId);

        mockMvc.perform(get("/api/cars/{listingId}/contact-messages", listingId)
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].listingTitle").value("Volkswagen Golf 2018"));
    }

    @Test
    void shouldRejectOtherUserReadingListingMessages() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-contact@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);
        String otherToken = registerUserAndExtractToken("other-contact@test.com", "Other");
        createContactMessage(listingId);

        mockMvc.perform(get("/api/cars/{listingId}/contact-messages", listingId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidContactMessage() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-contact@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);

        mockMvc.perform(post("/api/cars/{listingId}/contact", listingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contactName": "",
                                  "contactEmail": "not-email",
                                  "message": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectOwnerContactingOwnListing() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-contact@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);

        mockMvc.perform(post("/api/cars/{listingId}/contact", listingId)
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactMessageJson()))
                .andExpect(status().isBadRequest());
    }

    private void createContactMessage(Long listingId) throws Exception {
        mockMvc.perform(post("/api/cars/{listingId}/contact", listingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contactMessageJson()))
                .andExpect(status().isCreated());
    }

    private String contactMessageJson() {
        return """
                {
                  "contactName": "Buyer Visitor",
                  "contactEmail": "buyer@test.com",
                  "contactPhone": "600000000",
                  "message": "Estoy interesado en este coche."
                }
                """;
    }

    private String registerUserAndExtractToken(String email, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "password123"
                                }
                                """.formatted(name, email)))
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
