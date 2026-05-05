# AutoValor Backend - Checklist final

## Tests automaticos

```bash
./gradlew clean test
```

La suite completa debe pasar antes de entregar o desplegar.

## Comprobaciones manuales recomendadas

### Salud y documentacion

- `GET /api/health` devuelve `status = UP`.
- `GET /v3/api-docs` devuelve OpenAPI JSON.
- `GET /swagger-ui` abre la documentacion visual.

### Autenticacion

- Registrar usuario normal.
- Iniciar sesion.
- Usar token Bearer en rutas privadas.
- Acceder como admin.
- Verificar que un usuario normal no puede entrar en `/api/admin/**`.

### Anuncios

- Crear anuncio autenticado.
- Listar anuncios publicos.
- Buscar por texto, marca y filtros.
- Editar anuncio propio.
- Cambiar estado del anuncio.
- Borrar anuncio propio.

### Imagenes

- Subir imagen a anuncio propio.
- Ver imagenes publicamente.
- Rechazar formatos no permitidos.
- Borrar imagen propia.

### Favoritos

- Agregar favorito.
- Listar favoritos.
- Consultar estado de favorito.
- Quitar favorito.

### Contacto

- Enviar mensaje como visitante.
- Ver mensajes como propietario del anuncio.
- Ver mensajes como admin.
- Verificar que otro usuario no puede leer mensajes ajenos.

### Perfil

- Ver perfil.
- Editar nombre y email.
- Actualizar credencial de acceso.
- Ver mis anuncios.
- Ver mis favoritos.
- Borrar cuenta.

### Admin

- Ver estadisticas.
- Listar usuarios.
- Cambiar rol de usuario.
- Listar anuncios.
- Ocultar anuncio.
- Borrar anuncio.
- Ver mensajes de contacto.

### IA

- Enviar fotos y prompt a `/api/ai/vehicle-suggestions` con usuario autenticado.
- Revisar que devuelve sugerencias y avisos.
- Confirmar que no crea anuncios automaticamente.

## Pendientes futuros opcionales

- Mover imagenes a almacenamiento externo.
- Integracion completa con frontend.
- Mejoras visuales del panel admin.
- Rate limiting avanzado.
- Refresh tokens.
