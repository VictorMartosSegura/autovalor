# AutoValor Backend - Deployment

## Requirements

- Java 21, or Docker
- PostgreSQL database
- Environment variables configured in the hosting platform

## Main environment variables

| Variable | Description |
| --- | --- |
| `DB_URL` | PostgreSQL JDBC URL. Example format: `jdbc:postgresql://HOST:PORT/defaultdb?sslmode=require` |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Long random JWT secret, at least 32 characters |
| `JWT_EXPIRATION_MINUTES` | JWT duration in minutes. Default: `1440` |
| `ADMIN_NAME` | Initial admin display name |
| `ADMIN_EMAIL` | Initial admin email |
| `ADMIN_PASSWORD` | Initial admin password |
| `UPLOAD_DIR` | Folder where uploaded listing images are stored |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend URLs allowed by CORS |
| `PORT` | Server port. Default: `8080` |
| `SPRING_PROFILES_ACTIVE` | Use `prod` in production |

## Run locally

```bash
./gradlew bootRun
```

## Run tests

```bash
./gradlew clean test
```

## Build JAR

```bash
./gradlew clean bootJar
```

## Run with Docker

```bash
docker build -t autovalor-backend .
docker run --env-file .env -p 8080:8080 autovalor-backend
```

## Useful URLs

- Health check: `GET /api/health`
- API docs JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui`

## Deployment notes

- Do not commit `.env` with real credentials.
- In production, configure `CORS_ALLOWED_ORIGINS` with the real frontend URL.
- If the hosting platform has ephemeral disk storage, uploaded images may be lost after redeploy. For production, a future improvement is moving uploads to external storage such as S3, Cloudinary or similar.
