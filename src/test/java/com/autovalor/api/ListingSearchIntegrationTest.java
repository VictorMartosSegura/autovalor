package com.autovalor.api;

import static org.hamcrest.Matchers.hasSize;
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
        "spring.datasource.url=jdbc:h2:mem:listing_search_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-search-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin-search@autovalor.test",
        "app.admin.password=AdminSearch123"
})
class ListingSearchIntegrationTest {

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
    void shouldSearchByBrand() throws Exception {
        String token = registerUserAndExtractToken();
        createListing(token, "Volkswagen Golf", "Volkswagen", "Golf", 12500, 2018, 105000, "Diesel", "Manual");
        createListing(token, "Toyota Corolla", "Toyota", "Corolla", 15000, 2020, 60000, "Gasolina", "Automatico");

        mockMvc.perform(get("/api/cars")
                        .param("brand", "Volkswagen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].brand").value("Volkswagen"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldSearchByTextQuery() throws Exception {
        String token = registerUserAndExtractToken();
        createListing(token, "Volkswagen Golf", "Volkswagen", "Golf", 12500, 2018, 105000, "Diesel", "Manual");
        createListing(token, "Toyota Corolla", "Toyota", "Corolla", 15000, 2020, 60000, "Gasolina", "Automatico");

        mockMvc.perform(get("/api/cars")
                        .param("q", "corolla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].model").value("Corolla"));
    }

    @Test
    void shouldFilterByPriceAndYearRange() throws Exception {
        String token = registerUserAndExtractToken();
        createListing(token, "Volkswagen Golf", "Volkswagen", "Golf", 12500, 2018, 105000, "Diesel", "Manual");
        createListing(token, "Toyota Corolla", "Toyota", "Corolla", 15000, 2020, 60000, "Gasolina", "Automatico");
        createListing(token, "BMW Serie 3", "BMW", "Serie 3", 25000, 2021, 45000, "Diesel", "Automatico");

        mockMvc.perform(get("/api/cars")
                        .param("minPrice", "13000")
                        .param("maxPrice", "26000")
                        .param("minYear", "2020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldFilterByFuelTypeAndTransmission() throws Exception {
        String token = registerUserAndExtractToken();
        createListing(token, "Volkswagen Golf", "Volkswagen", "Golf", 12500, 2018, 105000, "Diesel", "Manual");
        createListing(token, "BMW Serie 3", "BMW", "Serie 3", 25000, 2021, 45000, "Diesel", "Automatico");

        mockMvc.perform(get("/api/cars")
                        .param("fuelType", "Diesel")
                        .param("transmission", "Automatico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].brand").value("BMW"));
    }

    @Test
    void shouldPaginateResults() throws Exception {
        String token = registerUserAndExtractToken();
        createListing(token, "Car 1", "Brand", "One", 10000, 2018, 100000, "Diesel", "Manual");
        createListing(token, "Car 2", "Brand", "Two", 11000, 2019, 90000, "Diesel", "Manual");
        createListing(token, "Car 3", "Brand", "Three", 12000, 2020, 80000, "Diesel", "Manual");

        mockMvc.perform(get("/api/cars")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void shouldSortByPriceAscending() throws Exception {
        String token = registerUserAndExtractToken();
        createListing(token, "Expensive", "BMW", "Serie 3", 25000, 2021, 45000, "Diesel", "Automatico");
        createListing(token, "Cheap", "Volkswagen", "Golf", 12500, 2018, 105000, "Diesel", "Manual");

        mockMvc.perform(get("/api/cars")
                        .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title").value("Cheap"))
                .andExpect(jsonPath("$.content[1].title").value("Expensive"));
    }

    private String registerUserAndExtractToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Search Seller",
                                  "email": "search-seller@test.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return result.getResponse().getContentAsString()
                .replaceAll(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    private void createListing(
            String token,
            String title,
            String brand,
            String model,
            int price,
            int year,
            int km,
            String fuelType,
            String transmission
    ) throws Exception {
        mockMvc.perform(post("/api/cars")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "Coche para prueba de busqueda",
                                  "price": %d,
                                  "brand": "%s",
                                  "model": "%s",
                                  "year": %d,
                                  "km": %d,
                                  "fuelType": "%s",
                                  "transmission": "%s"
                                }
                                """.formatted(title, price, brand, model, year, km, fuelType, transmission)))
                .andExpect(status().isCreated());
    }
}
