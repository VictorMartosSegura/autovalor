package com.autovalor.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autovalor.api.repository.ListingImageRepository;
import com.autovalor.api.repository.ListingRepository;
import com.autovalor.user.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:listing_image_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=autovalor-image-test-jwt-secret-with-more-than-32-characters",
        "app.jwt.expiration-minutes=1440",
        "app.admin.name=AutoValor Admin",
        "app.admin.email=admin-image@autovalor.test",
        "app.admin.password=AdminImage123",
        "app.upload.max-images-per-listing=6",
        "app.cloudinary.cloud-name=test-cloud",
        "app.cloudinary.api-key=test-key",
        "app.cloudinary.api-secret=test-secret",
        "app.cloudinary.folder=autovalor/test-listings"
})
@AutoConfigureMockMvc
class ListingImageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListingImageRepository listingImageRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private Cloudinary cloudinary;

    @MockitoBean
    private Uploader uploader;

    @BeforeEach
    void setUp() throws Exception {
        listingImageRepository.deleteAll();
        listingRepository.deleteAll();
        userRepository.deleteAll();

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/test-cloud/image/upload/autovalor/test-listings/car.png",
                "public_id", "autovalor/test-listings/car"
        ));
        when(uploader.destroy(any(String.class), anyMap())).thenReturn(Map.of("result", "ok"));
    }

    @Test
    void shouldUploadImageToOwnListing() throws Exception {
        String token = registerUserAndExtractToken("seller-image@test.com");
        Long listingId = createListingAndExtractId(token);

        mockMvc.perform(multipart("/api/cars/{listingId}/images", listingId)
                        .file(testImage())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.listingId").value(listingId))
                .andExpect(jsonPath("$.url", notNullValue()))
                .andExpect(jsonPath("$.contentType").value("image/png"));
    }

    @Test
    void shouldListListingImagesPublicly() throws Exception {
        String token = registerUserAndExtractToken("seller-image@test.com");
        Long listingId = createListingAndExtractId(token);
        uploadImageAndExtractId(token, listingId);

        mockMvc.perform(get("/api/cars/{listingId}/images", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].contentType").value("image/png"));
    }

    @Test
    void shouldDeleteOwnListingImage() throws Exception {
        String token = registerUserAndExtractToken("seller-image@test.com");
        Long listingId = createListingAndExtractId(token);
        Long imageId = uploadImageAndExtractId(token, listingId);

        mockMvc.perform(delete("/api/cars/{listingId}/images/{imageId}", listingId, imageId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cars/{listingId}/images", listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldRejectUploadWithoutToken() throws Exception {
        String token = registerUserAndExtractToken("seller-image@test.com");
        Long listingId = createListingAndExtractId(token);

        mockMvc.perform(multipart("/api/cars/{listingId}/images", listingId)
                        .file(testImage()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnsupportedImageFormat() throws Exception {
        String token = registerUserAndExtractToken("seller-image@test.com");
        Long listingId = createListingAndExtractId(token);
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not an image".getBytes()
        );

        mockMvc.perform(multipart("/api/cars/{listingId}/images", listingId)
                        .file(textFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    private MockMultipartFile testImage() {
        return new MockMultipartFile(
                "file",
                "car.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3, 4}
        );
    }

    private String registerUserAndExtractToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Seller Image",
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

    private Long uploadImageAndExtractId(String token, Long listingId) throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/cars/{listingId}/images", listingId)
                        .file(testImage())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();

        return Long.valueOf(result.getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":([0-9]+).*", "$1"));
    }
}
