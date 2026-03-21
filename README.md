# devsu-account

API Spring Boot para cuentas: Postgres (JPA), Redis (locks distribuidos), Kafka (eventos) y MongoDB (value storage).

## Requisitos

| Entorno | Necesitas |
|--------|-----------|
| **Docker (recomendado)** | Docker 24+ y Docker Compose v2 |
| **Solo JVM** | JDK 17+, Maven (o `./mvnw`), y servicios externos según perfil (ver abajo) |

## Datos iniciales (`BaseDatos.sql`)

Al arranque, si la tabla `client_account` está **vacía**, se ejecuta `src/main/resources/BaseDatos.sql` (cuentas demo). Para desactivar: `app.database.seed.enabled=false` (por defecto está en `true` en `application.properties`; en tests se desactiva con el perfil `test`).

## Habilitar con Docker (stack completo)

Desde la raíz del repo:

```bash
cp .env.example .env    # opcional: perfiles, puertos, credenciales
docker compose up --build
```

Cuando los contenedores estén arriba:

| Qué | Valor por defecto |
|-----|-------------------|
| API | <http://localhost:8096> |
| Salud | `curl -s http://localhost:8096/ping` → `pong` |
| Puerto API | `8096` — cámbialo con `SERVER_PORT` en `.env` |

Servicios que levanta Compose:

| Servicio | Rol | Puerto host (defecto) |
|----------|-----|------------------------|
| `api` | aplicación | 8096 |
| `postgres` | datos relacionales | 5432 |
| `redis` | locks (`prod`) | 6379 |
| `mongo` | value storage | 27017 |
| `kafka` | cola (Redpanda) | 9094 |

### Ambas APIs (account + user) con una sola infraestructura

Con `devsu-user` como repo **hermano** (misma carpeta padre que `devsu-account`):

```bash
cd ../devsu-compose
docker compose up --build
```

- Cuentas: **8096** — Usuarios: **8095**  
- **devsu-user** no usa Kafka (solo Postgres + Redis en ese stack); Kafka y Mongo siguen siendo para **devsu-account**.  
- Detalle: `../devsu-compose/README.md`

## Habilitar sin Docker (desarrollo local)

1. Arranca dependencias que use tu perfil (para algo cercano a prod: Postgres, Redis, Mongo y un broker Kafka en los puertos que indique tu configuración).
2. Exporta variables o usa `application-local.properties` / perfil adecuado.
3. Ejecuta:

```bash
./mvnw spring-boot:run
```

Por defecto el perfil activo sigue la prioridad descrita en `application.properties` y `ScopeUtils` (suele acabar en **`local`** si no defines `SPRING_PROFILES_ACTIVE`).

**Tests:**

```bash
./mvnw test
```

**JAR sin contenedor:**

```bash
./mvnw -q -DskipTests package
java -jar target/devsu-account-*.jar
```

## Perfiles Spring (`SPRING_PROFILES_ACTIVE`)

Mismo criterio que **devsu-user** (misma prioridad en `application.properties`, mismo `ScopeUtils` y mismos `@Profile` en lock/Redis/debug):

| Perfil | Uso típico | Comportamiento principal |
|--------|------------|---------------------------|
| **`prod`** | Contenedor con este `docker-compose` | Lock **Redis**, JDBC **Postgres**, Kafka, Mongo |
| **`local`** | IDE / máquina local | Lock en **memoria** (`LockServiceLocal`); H2 + servicios opcionales en `application-local.properties` |
| **`test`** | CI / tests (`@ActiveProfiles("test")`) | Lock en memoria, H2 `create-drop`, `application-test.properties` |

Prioridad del perfil activo: **`SPRING_PROFILES_ACTIVE`** (env / estándar Spring) → **`SCOPE_SUFFIX`** (derivado de `SCOPE`, ver `ScopeUtils.calculateScopeSuffix()`) → **`local`**.

Ejemplos con Compose:

```bash
SPRING_PROFILES_ACTIVE=prod docker compose up --build
SPRING_PROFILES_ACTIVE=local docker compose up --build
```

Variables relevantes en contenedor: ver `docker-compose.yml` y plantilla **`.env.example`**. Fuera de este repo, en producción real, revisa sobre todo `SPRING_DATASOURCE_*`, `REDIS_HOST`, `SPRING_KAFKA_BOOTSTRAP_SERVERS`, `SPRING_MONGODB_URI`.

## Imagen Docker sin Compose

```bash
./mvnw -q -DskipTests package
docker build -t devsu-account:local .
docker run --rm -p 8096:8096 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/devsu \
  -e SPRING_DATASOURCE_USERNAME=devsu \
  -e SPRING_DATASOURCE_PASSWORD=devsu \
  -e REDIS_HOST=host.docker.internal \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9094 \
  -e SPRING_MONGODB_URI=mongodb://user:pass@host.docker.internal:27017/db?authSource=admin \
  devsu-account:local
```

Ajusta hosts/puertos a tu red; en Linux `host.docker.internal` puede requerir `--add-host=host.docker.internal:host-gateway`.

## Build Maven

```bash
./mvnw test
./mvnw -DskipTests package
```
