# URL Shortener (Spring Boot + PostgreSQL)

A production-ready URL shortening service built with Spring Boot 3, Java 21, and PostgreSQL. It provides REST APIs for creating and retrieving shortened links plus first-class redirects that track hit counts and access timestamps.

---

## Features

- Generate random slugs or accept user-provided custom slugs (validated & unique)
- Secure slug generation using `SecureRandom`
- PostgreSQL persistence via Spring Data JPA (automatic schema management)
- Transparent redirect endpoint (`GET /{slug}`) that increments hit counters
- Optional link expiration (`expiresAt`) enforcing `410 Gone` redirects after expiry
- Detailed REST responses and consistent ProblemDetail error payloads
- Separate application/test profiles (H2 in-memory database for tests)
- Configurable base URL and slug length via properties or environment variables

---

## Tech Stack

- Java 21, Spring Boot 3.5.x
- Spring Web, Spring Data JPA, Bean Validation
- PostgreSQL (runtime) + H2 (tests)
- Maven Wrapper (`mvnw`)

---

## Prerequisites

1. **JDK 21** (Temurin, Oracle, etc.)
2. **Maven** (optional, wrapper is included)
3. **PostgreSQL 16+** – either a local install **or** the provided Docker container
4. **pgAdmin** or `psql` (recommended for DB management)
5. **Docker Desktop / Docker Compose** (only if you prefer running PostgreSQL in a container)

---

## Database Setup

### Option A: Docker Compose (recommended)

1. Ensure Docker Desktop (or any Docker Engine) is running.
2. (Optional) Export your preferred credentials before starting the container (PowerShell example):
   ```powershell
   $env:POSTGRES_DB="url_shortener"
   $env:POSTGRES_USER="url_shortener_app"
   $env:POSTGRES_PASSWORD="super-secret"
   $env:POSTGRES_PORT="5432"
   ```
3. From the project root run:
   ```powershell
   docker compose up -d postgres
   ```
   This launches the database defined in `docker-compose.yml`, exposes port `5432`, and stores data inside the named volume `postgres-data`.
4. Check health/status:
   ```powershell
   docker compose ps
   docker compose logs -f postgres
   ```
5. When you are done developing:
   ```powershell
   docker compose down       # stop the container
   docker compose down -v    # stop + delete the persisted volume (warning: wipes data)
   ```

> The Spring Boot app will automatically connect using the same defaults (`url_shortener_app` / `url_shortener` / `change-me`) unless you override `DATABASE_*` environment variables.

### Option B: Manual PostgreSQL install

1. Install/start PostgreSQL and connect as superuser.
2. Create database and application role:
   ```sql
   CREATE DATABASE url_shortener;
   CREATE USER url_shortener_app WITH PASSWORD 'choose-a-strong-password';
   GRANT ALL PRIVILEGES ON DATABASE url_shortener TO url_shortener_app;
   \c url_shortener
   GRANT USAGE, CREATE ON SCHEMA public TO url_shortener_app;
   GRANT ALL PRIVILEGES ON ALL TABLES    IN SCHEMA public TO url_shortener_app;
   GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO url_shortener_app;
   ```
3. (Optional) Make the app user owner of the DB/schema:
   ```sql
   ALTER DATABASE url_shortener OWNER TO url_shortener_app;
   ALTER SCHEMA public OWNER TO url_shortener_app;
   ```
4. Confirm connectivity:
   ```bash
   psql -U url_shortener_app -d url_shortener -h localhost
   ```

---

## Configuration

Default configuration lives in `src/main/resources/application.properties`. Each property can be overridden via environment variables; the values below show the defaults that ship with the project:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/url_shortener
spring.datasource.username=url_shortener_app
spring.datasource.password=CHANGE_ME

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

app.shortener.base-url=http://localhost:8080
app.shortener.slug-length=8
```

### Environment Variable Overrides

| Property                       | Env Var                | Default                                          |
|--------------------------------|------------------------|--------------------------------------------------|
| `spring.datasource.url`        | `DATABASE_URL`         | `jdbc:postgresql://localhost:5432/url_shortener` |
| `spring.datasource.username`   | `DATABASE_USERNAME`    | `url_shortener_app`                              |
| `spring.datasource.password`   | `DATABASE_PASSWORD`    | `change-me`                                      |
| `app.shortener.base-url`       | `SHORTENER_BASE_URL`   | `http://localhost:8080`                          |
| `app.shortener.slug-length`    | `SHORTENER_SLUG_LENGTH`| `8`                                              |

> Tip: when you run `docker compose up`, reuse the same values for both the `POSTGRES_*` variables (container) and `DATABASE_*` variables (Spring Boot) so the application can connect without additional configuration.

Example (PowerShell):

```powershell
setx DATABASE_URL "jdbc:postgresql://localhost:5432/url_shortener"
setx DATABASE_USERNAME "url_shortener_app"
setx DATABASE_PASSWORD "super-secret"
setx SHORTENER_BASE_URL "https://sho.rt"
```

Restart the terminal/session after running `setx`.

---

## Running the Application

```powershell
cd C:\Users\mehta\OneDrive\Documents\Projects\url-shortener
.\mvnw.cmd spring-boot:run
```

Or build and run the jar:

```bash
./mvnw clean package
java -jar target/ishumehta-0.0.1-SNAPSHOT.jar
```

The service listens on port `8080` by default. Change it via `server.port` in `application.properties` or `SERVER_PORT` env var.

---

## API Reference

| Method | Endpoint             | Description                               |
|--------|----------------------|-------------------------------------------|
| POST   | `/api/urls`          | Create a short URL                        |
| GET    | `/api/urls/{slug}`   | Retrieve metadata for a slug              |
| GET    | `/{slug}`            | Redirect to the long URL (HTTP 308) or `410 Gone` if expired |

### Create Short URL

```http
POST /api/urls
Content-Type: application/json

{
  "destinationUrl": "https://spring.io/projects",
  "customSlug": "spring",
  "expiresAt": "2025-12-31T23:59:59Z"
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "slug": "spring",
  "destinationUrl": "https://spring.io/projects",
  "shortUrl": "http://localhost:8080/spring",
  "hitCount": 0,
  "createdAt": "2025-11-18T10:05:33.914Z",
  "lastAccessedAt": null,
  "expiresAt": "2025-12-31T23:59:59Z"
}
```

- Omit `customSlug` to auto-generate a random slug (length configurable).
- Errors:
  - `400 Bad Request`: invalid URL or slug format (ProblemDetail payload with `errors` map)
  - `409 Conflict`: slug already exists

### Retrieve Metadata

```http
GET /api/urls/spring
```

Returns the same payload as creation with updated `hitCount` and `lastAccessedAt`.

### Redirect

```http
GET /spring
```

Responds with `308 Permanent Redirect` and a `Location` header pointing to the destination URL. Increments `hitCount` and stamps `lastAccessedAt`.

---

## Database Schema

`short_urls` table (managed by JPA):

| Column            | Type                      | Notes                             |
|-------------------|---------------------------|-----------------------------------|
| `id`              | BIGINT (identity)         | Primary key                       |
| `slug`            | VARCHAR(64) (unique)      | Short code                        |
| `destination_url` | VARCHAR(2048)             | Full URL                          |
| `created_at`      | TIMESTAMP WITH TIME ZONE  | Auto-set on insert                |
| `last_accessed_at`| TIMESTAMP WITH TIME ZONE  | Updated on redirect               |
| `hit_count`       | BIGINT                    | Incremented per redirect          |
| `expires_at`      | TIMESTAMP WITH TIME ZONE  | Optional expiry; links return `410 Gone` after this instant |

Use pgAdmin’s *View/Edit Data* or run:

```sql
SELECT id, slug, destination_url, hit_count, created_at, last_accessed_at
FROM short_urls
ORDER BY created_at DESC;
```

---

## Testing

Unit/integration tests run against H2 using the `test` profile:

```bash
./mvnw test
```

- `@SpringBootTest` + `@ActiveProfiles("test")`
- `src/test/resources/application-test.properties` configures the in-memory database.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `permission denied for schema public` | App user lacks schema privileges | Run the GRANT statements listed in “Database Setup” |
| `Port 8080 was already in use` | Another process still bound | `netstat -ano | findstr :8080` → `taskkill /PID <pid> /F`, or set `server.port` to a free port |
| Validation error on POST | Missing/invalid payload fields | Ensure `destinationUrl` starts with `http/https`, custom slugs match `[A-Za-z0-9_-]{3,64}`, and `expiresAt` is a future ISO-8601 timestamp |
| Duplicate slug error | Conflict with existing slug | Choose a different custom slug or omit the field to auto-generate |

---

## Next Steps / Enhancements

- Add authentication & rate limiting
- Build a small React/Vue frontend for managing links
- Extend analytics (unique visitors, per-day stats)
- Support link expiration or soft deletes
- Add integration tests covering REST controllers and redirects

---

## Useful Commands

```bash
# Run app
./mvnw spring-boot:run

# Build jar
./mvnw clean package

# Run tests
./mvnw test

# cURL shortcuts
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"destinationUrl":"https://example.com"}'

curl -I http://localhost:8080/abc123   # inspect redirect headers
```

Enjoy shortening URLs! Feel free to open issues or extend the service as needed.

