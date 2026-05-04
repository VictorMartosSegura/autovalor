package com.autovalor.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        "spring.datasource.url=jdbc:h2:mem:user_profile_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-profile-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin-profile@autovalor.test",
        "app.admin.password=AdminProfile123"
})
class UserProfileIntegrationTest {

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
    void shouldUpdateCurrentUserProfile() throws Exception {
        String token = registerUserAndExtractToken("profile@test.com", "Old Name");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "New Name",
                                  "email": "new-profile@test.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.email").value("new-profile@test.com"));
    }

    @Test
    void shouldRejectDuplicatedProfileEmail() throws Exception {
        registerUserAndExtractToken("existing-profile@test.com", "Existing");
        String token = registerUserAndExtractToken("profile@test.com", "Profile");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Profile",
                                  "email": "existing-profile@test.com"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldChangeCurrentUserPassword() throws Exception {
        String token = registerUserAndExtractToken("profile@test.com", "Profile");

        mockMvc.perform(patch("/api/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentSecret": "password123",
                                  "newSecret": "newPassword123"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "profile@test.com",
                                  "password": "newPassword123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("profile@test.com"));
    }

    @Test
    void shouldRejectPasswordChangeWithWrongCurrentPassword() throws Exception {
        String token = registerUserAndExtractToken("profile@test.com", "Profile");

        mockMvc.perform(patch("/api/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentSecret": "wrong-password",
                                  "newSecret": "newPassword123"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnMyListings() throws Exception {
        String token = registerUserAndExtractToken("profile@test.com", "Profile");
        createListingAndExtractId(token);

        mockMvc.perform(get("/api/users/me/listings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Volkswagen Golf 2018"));
    }

    @Test
    void shouldReturnMyFavorites() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-profile@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);
        String buyerToken = registerUserAndExtractToken("buyer-profile@test.com", "Buyer");

        mockMvc.perform(post("/api/favorites/{listingId}", listingId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me/favorites")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].listingId").value(listingId));
    }

    @Test
    void shouldDeleteCurrentUserAccount() throws Exception {
        String token = registerUserAndExtractToken("profile@test.com", "Profile");

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "profile@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectProfileWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
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
