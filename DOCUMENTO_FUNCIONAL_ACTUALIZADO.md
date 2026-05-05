# Documento funcional actualizado - AutoValor

**Alumno:** Victor Martos Segura  
**Proyecto:** AutoValor - Aplicacion de compra y venta de coches con IA  
**Version:** 1.0 actualizada segun backend implementado

## 1. Introduccion y contexto

AutoValor es una plataforma para publicar, buscar y gestionar anuncios de vehiculos de ocasion. El objetivo es mejorar la calidad de los anuncios y facilitar la compraventa de coches mediante formularios completos, imagenes, filtros avanzados y un modulo de IA que sugiere datos del vehiculo a partir de fotos y un prompt.

El backend actual esta implementado con Java 21, Spring Boot 4, Spring Security, JWT, PostgreSQL en Aiven, JPA, Docker, OpenAPI y tests de integracion.

## 2. Cambios respecto al documento funcional inicial

| Elemento | Planteamiento inicial | Situacion actual |
| --- | --- | --- |
| Backend | Node.js / JavaScript | Java 21 + Spring Boot 4 |
| Base de datos | No concretada o dependiente de APIs | PostgreSQL en Aiven con Spring Data JPA |
| IA | Modulo externo en Python | Endpoint backend preparado para IA de vision |
| Chat comprador-vendedor | Chat entre usuarios | Contacto/leads por formulario. Chat real como mejora futura |
| Publicacion con IA | Crear anuncio automatico | La IA solo sugiere datos; el usuario revisa antes de publicar |
| Documentacion API | No detallada | OpenAPI JSON + Swagger UI |

## 3. Requisitos funcionales

| Codigo | Requisito |
| --- | --- |
| RF1 | Registrar nuevos usuarios. |
| RF2 | Iniciar sesion con JWT. |
| RF3 | Diferenciar visitante, usuario registrado y administrador. |
| RF4 | Consultar y gestionar perfil de usuario. |
| RF5 | Crear, editar, borrar y cambiar estado de anuncios propios. |
| RF6 | Mostrar anuncios publicos a visitantes. |
| RF7 | Buscar anuncios por texto y filtros avanzados. |
| RF8 | Ordenar y paginar resultados. |
| RF9 | Subir y consultar imagenes de anuncios. |
| RF10 | Guardar y eliminar favoritos. |
| RF11 | Enviar mensajes de contacto al vendedor. |
| RF12 | Consultar leads recibidos como vendedor. |
| RF13 | Administrar usuarios, roles, anuncios, estadisticas y mensajes. |
| RF14 | Devolver errores globales con formato uniforme. |
| RF15 | Exponer documentacion OpenAPI y Swagger UI. |
| RF16 | Exponer health check para despliegue. |
| RF17 | Sugerir datos del vehiculo mediante IA usando fotos y prompt. |

## 4. Requisitos no funcionales

| Codigo | Requisito |
| --- | --- |
| RNF1 | Backend en Java 21 y Spring Boot 4. |
| RNF2 | Base de datos PostgreSQL en Aiven. |
| RNF3 | Seguridad con Spring Security y JWT. |
| RNF4 | Variables sensibles gestionadas mediante entorno. |
| RNF5 | CORS configurable para frontend separado. |
| RNF6 | Tests de integracion con H2 y MockMvc. |
| RNF7 | Dockerfile y configuracion de despliegue. |
| RNF8 | IA desactivada por defecto para no depender de servicios externos en tests. |

## 5. Roles

| Rol | Descripcion | Permisos |
| --- | --- | --- |
| Administrador | Usuario con control del sistema. | Gestionar usuarios, anuncios, mensajes, estadisticas y roles. |
| Usuario registrado | Usuario autenticado comprador o vendedor. | Crear anuncios, favoritos, perfil, imagenes, IA y contacto. |
| Visitante | Usuario no autenticado. | Ver anuncios publicos, buscar y enviar contacto. |

## 6. Casos de uso principales

| Codigo | Caso | Actor | Resultado |
| --- | --- | --- | --- |
| CU1 | Registro e inicio de sesion | Visitante | Usuario autenticado con JWT. |
| CU2 | Crear anuncio | Usuario | Anuncio publicado. |
| CU3 | Sugerir anuncio con IA | Usuario | Datos sugeridos para revisar. |
| CU4 | Buscar coche | Visitante/Usuario | Listado filtrado y paginado. |
| CU5 | Gestionar favoritos | Usuario | Favoritos actualizados. |
| CU6 | Contactar con vendedor | Visitante/Usuario | Lead guardado. |
| CU7 | Gestionar perfil | Usuario | Perfil actualizado. |
| CU8 | Moderar plataforma | Admin | Usuarios/anuncios/mensajes gestionados. |

## 7. Modelo de datos principal

- **User:** usuarios y administradores.
- **Listing:** anuncios de coches con campos basicos y avanzados.
- **ListingImage:** imagenes de anuncios.
- **Favorite:** favoritos de usuario.
- **ContactMessage:** mensajes/leads de contacto.

## 8. Endpoints principales

- Auth: `POST /api/auth/register`, `POST /api/auth/login`
- Usuario: `/api/users/me`, `/api/users/me/listings`, `/api/users/me/favorites`
- Anuncios: `/api/cars`, `/api/cars/{id}`, `/api/cars/{id}/status`
- Imagenes: `/api/cars/{listingId}/images`
- Favoritos: `/api/favorites`, `/api/favorites/{listingId}`
- Contacto: `/api/cars/{listingId}/contact`, `/api/contact-messages`
- IA: `POST /api/ai/vehicle-suggestions`
- Admin: `/api/admin/stats`, `/api/admin/users`, `/api/admin/listings`, `/api/admin/contact-messages`
- Sistema: `/api/health`, `/v3/api-docs`, `/swagger-ui`

## 9. Punto 15 - Checklist final de entrega

- Ejecutar `./gradlew clean test` y comprobar que todos los tests pasan.
- Probar registro, login y token Bearer.
- Verificar acceso de ADMIN y bloqueo a USER en `/api/admin/**`.
- Crear, listar, buscar, editar, ocultar y borrar anuncios.
- Subir, listar y borrar imagenes.
- Agregar, listar y quitar favoritos.
- Enviar contacto como visitante y leer mensajes como vendedor/admin.
- Gestionar perfil de usuario.
- Probar IA con fotos y prompt, comprobando que solo devuelve sugerencias.
- Abrir `/swagger-ui` y revisar `/v3/api-docs`.
- Probar `/api/health`.
- Configurar CORS con la URL real del frontend antes del despliegue.

## 10. Pendientes futuros

- Integracion frontend-backend completa.
- Despliegue real del backend.
- Almacenamiento externo de imagenes.
- Chat en tiempo real.
- Refresh tokens y rate limiting avanzado.
- Recomendaciones o anuncios destacados.
