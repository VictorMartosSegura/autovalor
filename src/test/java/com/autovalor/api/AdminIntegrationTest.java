package com.autovalor.api;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autovalor.api.repository.ContactMessageRepository;
import com.autovalor.api.repository.FavoriteRepository;
import com.autovalor.api.repository.ListingImageRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.Role;
import com.autovalor.user.User;
import com.autovalor.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-admin-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin-admin-test@autovalor.test",
        "app.admin.password=AdminAdmin123"
})
class AdminIntegrationTest {

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        contactMessageRepository.deleteAll();
        favoriteRepository.deleteAll();
        listingImageRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(new User(
                "Admin",
                "admin@test.com",
                passwordEncoder.encode("admin123"),
                Role.ADMIN
        ));
    }

    @Test
    void shouldRejectAdminEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAdminEndpointForNormalUser() throws Exception {
        String userToken = registerUserAndExtractToken("normal-admin-test@test.com", "Normal User");

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnAdminStats() throws Exception {
        String adminToken = loginAndExtractToken("admin@test.com", "admin123");
        String sellerToken = registerUserAndExtractToken("seller-admin-test@test.com", "Seller");
        createListingAndExtractId(sellerToken);

        mockMvc.perform(get("/api/admin/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalAdmins", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalListings").value(1))
                .andExpect(jsonPath("$.availableListings").value(1));
    }

    @Test
    void shouldListUsersWithoutPassword() throws Exception {
        String adminToken = loginAndExtractToken("admin@test.com", "admin123");
        registerUserAndExtractToken("normal-admin-test@test.com", "Normal User");

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void shouldUpdateUserRole() throws Exception {
        String adminToken = loginAndExtractToken("admin@test.com", "admin123");
        registerUserAndExtractToken("normal-admin-test@test.com", "Normal User");
        Long userId = userRepository.findByEmail("normal-admin-test@test.com").orElseThrow().getId();

        mockMvc.perform(patch("/api/admin/users/{userId}/role", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldListAndHideListingAsAdmin() throws Exception {
        String adminToken = loginAndExtractToken("admin@test.com", "admin123");
        String sellerToken = registerUserAndExtractToken("seller-admin-test@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);

        mockMvc.perform(get("/api/admin/listings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(listingId));

        mockMvc.perform(patch("/api/admin/listings/{listingId}/status", listingId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "HIDDEN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HIDDEN"));
    }

    @Test
    void shouldDeleteListingAsAdmin() throws Exception {
        String adminToken = loginAndExtractToken("admin@test.com", "admin123");
        String sellerToken = registerUserAndExtractToken("seller-admin-test@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);

        mockMvc.perform(delete("/api/admin/listings/{listingId}", listingId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cars/{listingId}", listingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListContactMessagesAsAdmin() throws Exception {
        String adminToken = loginAndExtractToken("admin@test.com", "admin123");
        String sellerToken = registerUserAndExtractToken("seller-admin-test@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);
        createContactMessage(listingId);

        mockMvc.perform(get("/api/admin/contact-messages")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].listingId").value(listingId))
                .andExpect(jsonPath("$[0].contactEmail").value("buyer@test.com"));
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

    private String loginAndExtractToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
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

    private void createContactMessage(Long listingId) throws Exception {
        mockMvc.perform(post("/api/cars/{listingId}/contact", listingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contactName": "Buyer Visitor",
                                  "contactEmail": "buyer@test.com",
                                  "contactPhone": "600000000",
                                  "message": "Estoy interesado en este coche."
                                }
                                """))
                .andExpect(status().isCreated());
    }
}
