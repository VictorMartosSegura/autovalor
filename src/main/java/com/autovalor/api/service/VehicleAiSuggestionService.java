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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VehicleAiSuggestionService {

    private static final Logger log = LoggerFactory.getLogger(VehicleAiSuggestionService.class);
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
            String responseBody = webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseBody == null || responseBody.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "La IA no ha devuelto respuesta");
            }

            JsonNode response = objectMapper.readTree(responseBody);
            String content = response.path("choices").path(0).path("message").path("content").asText();
            return parseSuggestion(content);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (WebClientResponseException exception) {
            log.error("OpenAI error while generating vehicle suggestion. Status: {}. Body: {}",
                    exception.getStatusCode(), sanitizeOpenAiBody(exception.getResponseBodyAsString()));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI devolvio un error. Revisa la consola del backend.");
        } catch (Exception exception) {
            log.error("Unexpected error while generating vehicle suggestion", exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo obtener la sugerencia de IA");
        }
    }

    private Map<String, Object> buildRequest(String prompt, List<MultipartFile> images) throws IOException {
        List<Map<String, Object>> content = new ArrayList<>();
        content.add(Map.of(
                "type", "text",
                "text", """
                        Eres un experto en vehiculos de segunda mano y en redaccion de anuncios profesionales.
                        Analiza las imagenes del vehiculo y el texto del usuario.
                        Devuelve unicamente JSON valido, sin markdown ni texto adicional.
                        Responde siempre en castellano.

                        Si no puedes determinar un dato con seguridad, devuelve null y anade un aviso en warnings.
                        No inventes kilometraje, ano, potencia, combustible, propietarios ni equipamiento.

                        Usa estos valores cerrados cuando correspondan:
                        fuelType: GASOLINE, DIESEL, ELECTRIC, HYBRID o null.
                        transmission: MANUAL, AUTOMATIC o null.
                        bodyType: SEDAN, SUV, HATCHBACK, COUPE, WAGON, VAN, CONVERTIBLE o null.
                        sellerType: PRIVATE, PROFESSIONAL o null.
                        environmentalLabel: ECO, ZERO, C, B o null.

                        Devuelve exactamente estos campos:
                        title, description, brand, model, year, km, fuelType, transmission, location,
                        province, sellerType, bodyType, doors, powerCv, engineSize, environmentalLabel,
                        warranty, color, registrationMonth, registrationYear, previousOwners, financeable,
                        maintenanceBook, confidence, warnings.

                        confidence debe ser un numero entre 0 y 1.
                        warnings debe ser un array de avisos para que el usuario revise datos dudosos.
                        El usuario revisara y editara la propuesta antes de publicar.

                        Orden recomendado de imagenes: frontal, trasera, lateral, interior, cuadro de mandos y extra.
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
        request.put("temperature", 0.1);
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

    private String sanitizeOpenAiBody(String body) {
        if (body == null || body.isBlank()) {
            return "<empty>";
        }
        return body.length() > 1000 ? body.substring(0, 1000) + "..." : body;
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
