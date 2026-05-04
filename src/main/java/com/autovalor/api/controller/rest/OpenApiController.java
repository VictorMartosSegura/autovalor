package com.autovalor.api.controller.rest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenApiController {

    @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> openApi() {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("openapi", "3.0.3");
        spec.put("info", Map.of(
                "title", "AutoValor API",
                "version", "1.0.0",
                "description", "API REST para marketplace de compraventa de coches AutoValor"
        ));
        spec.put("servers", List.of(Map.of("url", "http://localhost:8080", "description", "Local")));
        spec.put("tags", List.of(
                tag("Auth", "Registro, login y usuario autenticado"),
                tag("Users", "Perfil del usuario"),
                tag("Listings", "Anuncios de coches"),
                tag("Images", "Imagenes de anuncios"),
                tag("Favorites", "Favoritos"),
                tag("Contact", "Mensajes de contacto al vendedor"),
                tag("Admin", "Panel de administracion")
        ));
        spec.put("paths", paths());
        spec.put("components", components());
        return spec;
    }

    @GetMapping(value = {"/swagger-ui", "/swagger-ui.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public String swaggerUi() {
        return """
                <!doctype html>
                <html lang=\"es\">
                <head>
                    <meta charset=\"utf-8\">
                    <title>AutoValor API Docs</title>
                    <link rel=\"stylesheet\" href=\"https://unpkg.com/swagger-ui-dist@5/swagger-ui.css\">
                    <style>
                        body { margin: 0; background: #fafafa; }
                        .topbar { display: none; }
                    </style>
                </head>
                <body>
                    <div id=\"swagger-ui\"></div>
                    <script src=\"https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js\"></script>
                    <script>
                        window.onload = function () {
                            window.ui = SwaggerUIBundle({
                                url: '/v3/api-docs',
                                dom_id: '#swagger-ui',
                                deepLinking: true,
                                presets: [SwaggerUIBundle.presets.apis],
                                layout: 'BaseLayout'
                            });
                        };
                    </script>
                </body>
                </html>
                """;
    }

    private Map<String, Object> paths() {
        Map<String, Object> paths = new LinkedHashMap<>();

        paths.put("/api/auth/register", Map.of("post", operation("Auth", "Registrar usuario", false, "AuthResponse")));
        paths.put("/api/auth/login", Map.of("post", operation("Auth", "Iniciar sesion", false, "AuthResponse")));

        paths.put("/api/users/me", Map.of(
                "get", operation("Users", "Ver mi perfil", true, "UserResponse"),
                "put", operation("Users", "Actualizar mi perfil", true, "UserResponse"),
                "delete", noContentOperation("Users", "Borrar mi cuenta", true)
        ));
        paths.put("/api/users/me/password", Map.of("patch", noContentOperation("Users", "Cambiar mi password", true)));
        paths.put("/api/users/me/listings", Map.of("get", arrayOperation("Users", "Ver mis anuncios", true, "ListingResponse")));
        paths.put("/api/users/me/favorites", Map.of("get", arrayOperation("Users", "Ver mis favoritos", true, "FavoriteResponse")));

        paths.put("/api/cars", Map.of(
                "get", operation("Listings", "Buscar anuncios publicos con filtros", false, "ListingPageResponse"),
                "post", operation("Listings", "Crear anuncio", true, "ListingResponse")
        ));
        paths.put("/api/cars/{id}", Map.of(
                "get", operation("Listings", "Ver anuncio por id", false, "ListingResponse"),
                "put", operation("Listings", "Actualizar anuncio propio", true, "ListingResponse"),
                "delete", noContentOperation("Listings", "Borrar anuncio propio", true)
        ));
        paths.put("/api/cars/{id}/status", Map.of("patch", operation("Listings", "Cambiar estado de anuncio", true, "ListingResponse")));
        paths.put("/api/cars/{listingId}/images", Map.of(
                "get", arrayOperation("Images", "Listar imagenes de un anuncio", false, "ListingImageResponse"),
                "post", operation("Images", "Subir imagen a anuncio", true, "ListingImageResponse")
        ));
        paths.put("/api/cars/{listingId}/images/{imageId}", Map.of("delete", noContentOperation("Images", "Borrar imagen de anuncio", true)));

        paths.put("/api/favorites", Map.of("get", arrayOperation("Favorites", "Listar mis favoritos", true, "FavoriteResponse")));
        paths.put("/api/favorites/{listingId}", Map.of(
                "post", operation("Favorites", "Agregar favorito", true, "FavoriteStatusResponse"),
                "delete", operation("Favorites", "Quitar favorito", true, "FavoriteStatusResponse")
        ));
        paths.put("/api/favorites/{listingId}/status", Map.of("get", operation("Favorites", "Consultar estado de favorito", true, "FavoriteStatusResponse")));

        paths.put("/api/cars/{listingId}/contact", Map.of("post", operation("Contact", "Enviar mensaje al vendedor", false, "ContactMessageResponse")));
        paths.put("/api/contact-messages", Map.of("get", arrayOperation("Contact", "Ver mensajes recibidos", true, "ContactMessageResponse")));
        paths.put("/api/cars/{listingId}/contact-messages", Map.of("get", arrayOperation("Contact", "Ver mensajes de un anuncio", true, "ContactMessageResponse")));

        paths.put("/api/admin/stats", Map.of("get", operation("Admin", "Ver estadisticas", true, "AdminStatsResponse")));
        paths.put("/api/admin/users", Map.of("get", arrayOperation("Admin", "Listar usuarios", true, "AdminUserResponse")));
        paths.put("/api/admin/users/{userId}/role", Map.of("patch", operation("Admin", "Cambiar rol de usuario", true, "AdminUserResponse")));
        paths.put("/api/admin/listings", Map.of("get", arrayOperation("Admin", "Listar todos los anuncios", true, "ListingResponse")));
        paths.put("/api/admin/listings/{listingId}/status", Map.of("patch", operation("Admin", "Moderar estado de anuncio", true, "ListingResponse")));
        paths.put("/api/admin/listings/{listingId}", Map.of("delete", noContentOperation("Admin", "Borrar anuncio como admin", true)));
        paths.put("/api/admin/contact-messages", Map.of("get", arrayOperation("Admin", "Ver todos los mensajes", true, "ContactMessageResponse")));

        return paths;
    }

    private Map<String, Object> components() {
        Map<String, Object> schemas = new LinkedHashMap<>();
        schemas.put("AuthResponse", objectSchema("token", "tokenType", "id", "name", "email", "role"));
        schemas.put("UserResponse", objectSchema("id", "name", "email", "role", "createdAt"));
        schemas.put("ListingResponse", objectSchema("id", "title", "description", "price", "brand", "model", "year", "km", "fuelType", "transmission", "location", "province", "sellerType", "bodyType", "doors", "powerCv", "environmentalLabel", "status", "userId", "userName"));
        schemas.put("ListingPageResponse", objectSchema("content", "page", "size", "totalElements", "totalPages", "first", "last"));
        schemas.put("ListingImageResponse", objectSchema("id", "listingId", "fileName", "url", "contentType", "sizeBytes", "createdAt"));
        schemas.put("FavoriteResponse", objectSchema("id", "listingId", "listing", "createdAt"));
        schemas.put("FavoriteStatusResponse", objectSchema("listingId", "favorite", "favoritesCount"));
        schemas.put("ContactMessageResponse", objectSchema("id", "listingId", "listingTitle", "senderUserId", "contactName", "contactEmail", "contactPhone", "message", "createdAt"));
        schemas.put("AdminStatsResponse", objectSchema("totalUsers", "totalAdmins", "totalListings", "availableListings", "reservedListings", "soldListings", "hiddenListings", "totalFavorites", "totalContactMessages"));
        schemas.put("AdminUserResponse", objectSchema("id", "name", "email", "role", "createdAt"));
        schemas.put("ApiErrorResponse", objectSchema("status", "error", "message", "path", "timestamp", "fieldErrors"));

        return Map.of(
                "securitySchemes", Map.of(
                        "bearerAuth", Map.of(
                                "type", "http",
                                "scheme", "bearer",
                                "bearerFormat", "JWT"
                        )
                ),
                "schemas", schemas
        );
    }

    private Map<String, Object> operation(String tag, String summary, boolean secured, String schema) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("tags", List.of(tag));
        operation.put("summary", summary);
        if (secured) {
            operation.put("security", List.of(Map.of("bearerAuth", List.of())));
        }
        operation.put("responses", Map.of(
                "200", response("OK", schema),
                "400", response("Bad Request", "ApiErrorResponse"),
                "401", response("Unauthorized", "ApiErrorResponse"),
                "403", response("Forbidden", "ApiErrorResponse"),
                "404", response("Not Found", "ApiErrorResponse")
        ));
        return operation;
    }

    private Map<String, Object> arrayOperation(String tag, String summary, boolean secured, String schema) {
        Map<String, Object> operation = operation(tag, summary, secured, schema);
        operation.put("responses", Map.of("200", arrayResponse("OK", schema)));
        return operation;
    }

    private Map<String, Object> noContentOperation(String tag, String summary, boolean secured) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("tags", List.of(tag));
        operation.put("summary", summary);
        if (secured) {
            operation.put("security", List.of(Map.of("bearerAuth", List.of())));
        }
        operation.put("responses", Map.of("204", Map.of("description", "No Content")));
        return operation;
    }

    private Map<String, Object> response(String description, String schema) {
        return Map.of(
                "description", description,
                "content", Map.of("application/json", Map.of(
                        "schema", Map.of("$ref", "#/components/schemas/" + schema)
                ))
        );
    }

    private Map<String, Object> arrayResponse(String description, String schema) {
        return Map.of(
                "description", description,
                "content", Map.of("application/json", Map.of(
                        "schema", Map.of(
                                "type", "array",
                                "items", Map.of("$ref", "#/components/schemas/" + schema)
                        )
                ))
        );
    }

    private Map<String, String> tag(String name, String description) {
        return Map.of("name", name, "description", description);
    }

    private Map<String, Object> objectSchema(String... fields) {
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String field : fields) {
            properties.put(field, Map.of("type", "string"));
        }
        return Map.of("type", "object", "properties", properties);
    }
}
