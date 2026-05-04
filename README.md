# AutoValor Backend

Backend de AutoValor desarrollado con Java 21, Spring Boot, Spring Security, JWT, JPA y PostgreSQL.

## Requisitos

- Java 21
- PostgreSQL, en este proyecto se usa Aiven
- Gradle Wrapper incluido en el repositorio

## Configuracion de entorno

Copia el archivo de ejemplo:

```bash
cp .env.example .env
```

Rellena `.env` con tus datos reales. No subas nunca `.env` al repositorio.

Variables necesarias:

```env
DB_URL=jdbc:postgresql://HOST_AIVEN:PUERTO/NOMBRE_BD?sslmode=require
DB_USERNAME=avnadmin
DB_PASSWORD=tu_password_real

JWT_SECRET=clave_larga_y_segura_de_minimo_32_caracteres
JWT_EXPIRATION_MINUTES=1440

ADMIN_NAME=AutoValor Admin
ADMIN_EMAIL=admin@autovalor.com
ADMIN_PASSWORD=password_segura_del_admin
```

Importante: si una contrasena real se ha compartido por error en capturas, chats o commits, cambiala desde el panel de Aiven.

## Ejecutar en local

Exporta las variables de entorno o cargalas desde tu IDE y ejecuta:

```bash
./gradlew bootRun
```

## Autenticacion y roles

Roles disponibles:

- `ADMIN`: usuario administrador unico.
- `USER`: usuario registrado normal.
- Visitante: usuario no autenticado. No se guarda en la base de datos y solo puede acceder a rutas publicas.

El administrador se crea automaticamente al arrancar si defines `ADMIN_EMAIL` y `ADMIN_PASSWORD` y todavia no existe ningun admin.

## Endpoints de autenticacion

### Registro

```http
POST /api/auth/register
```

```json
{
  "name": "Victor",
  "email": "victor@test.com",
  "password": "password123"
}
```

### Login

```http
POST /api/auth/login
```

```json
{
  "email": "victor@test.com",
  "password": "password123"
}
```

### Usuario actual

```http
GET /api/users/me
Authorization: Bearer TU_TOKEN
```

## Seguridad

- `/api/auth/**` es publico.
- `GET /api/cars/**` es publico para visitantes.
- `/api/admin/**` requiere rol `ADMIN`.
- El resto de rutas requiere usuario autenticado.
