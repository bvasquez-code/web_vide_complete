# Web Video Complete

Primera version funcional para administrar y visualizar videos. El proyecto mantiene tres partes separadas:

- `database`: scripts MySQL.
- `backend`: API Java Spring Boot.
- `frontend`: aplicacion Angular.

## Arquitectura aplicada

- Base de datos: scripts rerunnable en `database/tables/table_<tabla>.sql`, encapsulados en procedures temporales `p_manage_<tabla>`, con columnas PascalCase, `Status`, auditoria e indices/FK.
- Backend: paquete base `com.ccadmin.app`, controllers con `ResponseWsDto`, services separados para busqueda/escritura, repositories JPA, entidades con campos publicos alineados a la base de datos.
- Frontend: Angular con `AppModule` unico, rutas centralizadas, servicios que consumen `ApiService`, modelos `Entity`/`Dto`, formularios con `ngModel`.

## Base de datos

Ejecutar los scripts en este orden sobre el schema MySQL seleccionado:

1. `database/tables/table_admin_user.sql`
2. `database/tables/table_video_category.sql`
3. `database/tables/table_actor.sql`
4. `database/tables/table_tag.sql`
5. `database/tables/table_video.sql`
6. `database/tables/table_video_category_rel.sql`
7. `database/tables/table_video_actor_rel.sql`
8. `database/tables/table_video_tag_rel.sql`
9. `database/tables/table_video_capture.sql`
10. `database/tables/table_video_view_log.sql`
11. `database/inserts/initial_video_data.sql`

Usuario inicial:

- Usuario: `admin`
- Contrasena: `admin123`

La contrasena se guarda como SHA-256 en `PasswordHash`. Es una seguridad simple para esta primera version; el servicio esta aislado para migrar luego a BCrypt u otro mecanismo.

## Backend

Configurar credenciales en:

```text
backend/src/main/resources/application-dev.properties
```

Ejecutar:

```bash
cd backend
mvn spring-boot:run
```

API local:

```text
http://localhost:8090
```

Endpoints principales:

- `GET /api/v1/public/categories`
- `GET /api/v1/public/videos/recent`
- `GET /api/v1/public/videos/mostViewed`
- `GET /api/v1/public/categories/{categoryCod}/videos`
- `GET /api/v1/public/videos/{videoCod}`
- `GET /api/v1/public/videos/{videoCod}/related`
- `POST /api/v1/public/videos/{videoCod}/view`
- `POST /api/v1/admin/auth/login`
- `GET/POST /api/v1/admin/videos/...`
- `GET/POST /api/v1/admin/categories/...`
- `GET/POST /api/v1/admin/actors/...`
- `GET/POST /api/v1/admin/tags/...`

## Frontend

Ejecutar:

```bash
cd frontend
npm install
npm start
```

URL local:

```text
http://localhost:4200
```

Pantallas:

- `/`: inicio publico con categorias y videos recientes/mas vistos.
- `/category/:categoryCod`: videos por categoria.
- `/video/:videoCod`: reproductor manual y relacionados.
- `/admin/login`: login administrativo.
- `/admin`: panel.
- `/admin/videos`: gestion de videos.
- `/admin/categories`: gestion de categorias.
- `/admin/actors`: gestion de actores.
- `/admin/tags`: gestion de tags.

## Alcance implementado

- Videos con origen `EMBED`, `URL` o `PATH`.
- Validaciones principales para titulo, origen, referencia y categoria obligatoria.
- Asociacion de videos con categorias, actores y tags.
- Activacion/inactivacion por `Status`.
- Contador simple de vistas con log en `video_view_log`.
- Relacionados por categorias compartidas.
- No se implementa carga fisica de archivos, streaming avanzado, comentarios, likes, pagos ni roles complejos.
