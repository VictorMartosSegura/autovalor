# AutoValor Backend

Backend de AutoValor desarrollado con Java 21, Spring Boot 4, Spring Security, JWT, JPA y PostgreSQL.

## Estado del backend

Funcionalidades principales implementadas:

- Login, registro, JWT y roles `ADMIN` / `USER`.
- Visitantes con acceso a rutas publicas.
- CRUD de anuncios de coches.
- Subida y listado de imagenes de anuncios.
- Favoritos.
- Busqueda, filtros y paginacion de anuncios.
- Contacto con vendedor / leads.
- Perfil de usuario.
- Panel de administracion.
- Validaciones y errores globales.
- Documentacion OpenAPI y Swagger UI.
- Health check, CORS y Docker.
- Sugerencia de datos de vehiculo con IA a partir de fotos y prompt.

## Requisitos

- Java 21
- PostgreSQL, en este proyecto se usa Aiven
- Gradle Wrapper incluido en el repositorio
- Docker opcional

## Configuracion de entorno

Copia el archivo de ejemplo:

```bash
cp .env.example .env
```

Rellena `.env` con tus datos reales. No subas nunca `.env` al repositorio.

Variables principales:

```env
DB_URL=jdbc:postgresql://HOST:PORT/defaultdb?sslmode=require
DB_USERNAME=YOUR_DB_USER
DB_PASSWORD=YOUR_DB_SECRET

JWT_SECRET=YOUR_LONG_RANDOM_JWT_SECRET_AT_LEAST_32_CHARS
JWT_EXPIRATION_MINUTES=1440

ADMIN_NAME=AutoValor Admin
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=YOUR_ADMIN_SECRET

UPLOAD_DIR=uploads
MAX_IMAGES_PER_LISTING=6

AI_ENABLED=false
OPENAI_API_KEY=YOUR_OPENAI_API_KEY
OPENAI_MODEL=gpt-4o-mini

CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

Importante: si una contrasena real se ha compartido por error en capturas, chats o commits, cambiala desde el panel del proveedor de base de datos.

## Ejecutar en local

```bash
./gradlew bootRun
```

## Ejecutar tests

```bash
./gradlew clean test
```

## Build

```bash
./gradlew clean bootJar
```

## Docker

```bash
docker build -t autovalor-backend .
docker run --env-file .env -p 8080:8080 autovalor-backend
```

## URLs utiles

- Health check: `GET /api/health`
- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui`

## Autenticacion y roles

Roles disponibles:

- `ADMIN`: usuario administrador.
- `USER`: usuario registrado normal.
- Visitante: usuario no autenticado. No se guarda en la base de datos y solo puede acceder a rutas publicas.

El administrador se crea automaticamente al arrancar si defines `ADMIN_EMAIL` y `ADMIN_PASSWORD` y todavia no existe ningun admin.

## Endpoints principales

### Auth

```http
POST /api/auth/register
POST /api/auth/login
```

### Usuario

```http
GET    /api/users/me
PUT    /api/users/me
PATCH  /api/users/me/password
DELETE /api/users/me
GET    /api/users/me/listings
GET    /api/users/me/favorites
```

### Anuncios

```http
GET    /api/cars
GET    /api/cars/{id}
POST   /api/cars
PUT    /api/cars/{id}
PATCH  /api/cars/{id}/status
DELETE /api/cars/{id}
```

### Imagenes

```http
GET    /api/cars/{listingId}/images
POST   /api/cars/{listingId}/images
DELETE /api/cars/{listingId}/images/{imageId}
```

### Favoritos

```http
GET    /api/favorites
POST   /api/favorites/{listingId}
DELETE /api/favorites/{listingId}
GET    /api/favorites/{listingId}/status
```

### Contacto con vendedor

```http
POST /api/cars/{listingId}/contact
GET  /api/contact-messages
GET  /api/cars/{listingId}/contact-messages
```

### IA

```http
POST /api/ai/vehicle-suggestions
```

Este endpoint recibe `multipart/form-data` con:

- `prompt`: texto opcional.
- `images`: una o varias fotos del vehiculo.

Devuelve una sugerencia de datos del anuncio. No crea el anuncio automaticamente.

### Admin

```http
GET    /api/admin/stats
GET    /api/admin/users
PATCH  /api/admin/users/{userId}/role
GET    /api/admin/listings
PATCH  /api/admin/listings/{listingId}/status
DELETE /api/admin/listings/{listingId}
GET    /api/admin/contact-messages
```

## Seguridad

- `/api/auth/**` es publico.
- `GET /api/cars/**` es publico para visitantes.
- `POST /api/cars/{listingId}/contact` es publico para permitir leads de visitantes.
- `/api/admin/**` requiere rol `ADMIN`.
- `/api/ai/**` requiere usuario autenticado.
- El resto de rutas requiere usuario autenticado.

## Notas

- Los datos devueltos por IA son sugerencias y deben ser revisados por el usuario.
- En despliegues con disco efimero, las imagenes subidas pueden perderse al redesplegar. Para produccion real se recomienda almacenamiento externo.
