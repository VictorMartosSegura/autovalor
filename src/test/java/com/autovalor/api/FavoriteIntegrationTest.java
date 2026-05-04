package com.autovalor.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        "spring.datasource.url=jdbc:h2:mem:favorite_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-favorite-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin-favorite@autovalor.test",
        "app.admin.password=AdminFavorite123"
})
class FavoriteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
        favoriteRepository.deleteAll();
        listingImageRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldAddFavorite() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-fav@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);
        String buyerToken = registerUserAndExtractToken("buyer-fav@test.com", "Buyer");

        mockMvc.perform(post("/api/favorites/{listingId}", listingId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listingId").value(listingId))
                .andExpect(jsonPath("$.favorite").value(true))
                .andExpect(jsonPath("$.favoritesCount").value(1));
    }

    @Test
    void shouldListMyFavorites() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-fav@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);
        String buyerToken = registerUserAndExtractToken("buyer-fav@test.com", "Buyer");
        addFavorite(buyerToken, listingId);

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].listingId").value(listingId))
                .andExpect(jsonPath("$[0].listing.title").value("Volkswagen Golf 2018"));
    }

    @Test
    void shouldReturnFavoriteStatus() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-fav@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);
        String buyerToken = registerUserAndExtractToken("buyer-fav@test.com", "Buyer");
        addFavorite(buyerToken, listingId);

        mockMvc.perform(get("/api/favorites/{listingId}/status", listingId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true))
                .andExpect(jsonPath("$.favoritesCount").value(1));
    }

    @Test
    void shouldRemoveFavorite() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-fav@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);
        String buyerToken = registerUserAndExtractToken("buyer-fav@test.com", "Buyer");
        addFavorite(buyerToken, listingId);

        mockMvc.perform(delete("/api/favorites/{listingId}", listingId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(false))
                .andExpect(jsonPath("$.favoritesCount").value(0));
    }

    @Test
    void shouldNotDuplicateFavorite() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-fav@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);
        String buyerToken = registerUserAndExtractToken("buyer-fav@test.com", "Buyer");

        addFavorite(buyerToken, listingId);
        addFavorite(buyerToken, listingId);

        mockMvc.perform(get("/api/favorites/{listingId}/status", listingId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true))
                .andExpect(jsonPath("$.favoritesCount").value(1));
    }

    @Test
    void shouldRejectFavoriteWithoutToken() throws Exception {
        String sellerToken = registerUserAndExtractToken("seller-fav@test.com", "Seller");
        Long listingId = createListingAndExtractId(sellerToken);

        mockMvc.perform(post("/api/favorites/{listingId}", listingId))
                .andExpect(status().isUnauthorized());
    }

    private void addFavorite(String token, Long listingId) throws Exception {
        mockMvc.perform(post("/api/favorites/{listingId}", listingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
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
