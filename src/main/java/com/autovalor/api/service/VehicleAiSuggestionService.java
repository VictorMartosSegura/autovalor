package com.autovalor.api.service;

import com.autovalor.api.dto.aiDTO.VehicleAiSuggestionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VehicleAiSuggestionService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final boolean enabled;

    public VehicleAiSuggestionService(
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.model:gpt-4o-mini}") String model,
            @Value("${app.ai.enabled:false}") boolean enabled
    ) {
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = enabled;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public VehicleAiSuggestionResponse suggest(String prompt, List<MultipartFile> images) {
        validateImages(images);

        if (!enabled || apiKey == null || apiKey.isBlank()) {
            return disabledResponse();
        }

        try {
            Map<String, Object> request = buildRequest(prompt, images);
            JsonNode response = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "La IA no ha devuelto respuesta");
            }

            String content = response.path("choices").path(0).path("message").path("content").asText();
            return parseSuggestion(content);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo obtener la sugerencia de IA");
        }
    }

    private Map<String, Object> buildRequest(String prompt, List<MultipartFile> images) throws IOException {
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of(
                "type", "text",
                "text", """
                        Analiza las fotos del vehiculo y el texto del usuario. Devuelve solo JSON valido, sin markdown.
                        No inventes datos que no puedan deducirse. Si no sabes un campo, usa null.
                        El usuario revisara los datos antes de publicar el anuncio.
                        Campos esperados: title, description, brand, model, year, km, fuelType, transmission,
                        location, province, sellerType, bodyType, doors, powerCv, engineSize, environmentalLabel,
                        warranty, color, registrationMonth, registrationYear, previousOwners, financeable,
                        maintenanceBook, confidence, warnings.
                        Prompt del usuario: %s
                        """.formatted(prompt == null ? "" : prompt)
        ));

        for (MultipartFile image : images) {
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            content.add(Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", "data:" + image.getContentType() + ";base64," + base64)
            ));
        }

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", content);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", model);
        request.put("messages", List.of(message));
        request.put("temperature", 0.2);
        request.put("response_format", Map.of("type", "json_object"));
        return request;
    }

    private VehicleAiSuggestionResponse parseSuggestion(String content) throws IOException {
        JsonNode node = objectMapper.readTree(content);
        return new VehicleAiSuggestionResponse(
                text(node, "title"),
                text(node, "description"),
                text(node, "brand"),
                text(node, "model"),
                integer(node, "year"),
                integer(node, "km"),
                text(node, "fuelType"),
                text(node, "transmission"),
                text(node, "location"),
                text(node, "province"),
                text(node, "sellerType"),
                text(node, "bodyType"),
                integer(node, "doors"),
                integer(node, "powerCv"),
                text(node, "engineSize"),
                text(node, "environmentalLabel"),
                bool(node, "warranty"),
                text(node, "color"),
                integer(node, "registrationMonth"),
                integer(node, "registrationYear"),
                integer(node, "previousOwners"),
                bool(node, "financeable"),
                bool(node, "maintenanceBook"),
                number(node, "confidence"),
                warnings(node)
        );
    }

    private VehicleAiSuggestionResponse disabledResponse() {
        return new VehicleAiSuggestionResponse(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0.0,
                List.of("La IA no esta activada. Configura AI_ENABLED=true y la clave de OpenAI para obtener sugerencias reales.")
        );
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes subir al menos una imagen");
        }
        if (images.size() > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Puedes subir como maximo 6 imagenes");
        }
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las imagenes no pueden estar vacias");
            }
            if (!ALLOWED_CONTENT_TYPES.contains(image.getContentType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no permitido");
            }
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Integer integer(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asInt();
    }

    private Boolean bool(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    private Double number(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asDouble();
    }

    private List<String> warnings(JsonNode node) {
        JsonNode warningsNode = node.get("warnings");
        if (warningsNode == null || !warningsNode.isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        warningsNode.forEach(item -> result.add(item.asText()));
        return result;
    }
}
