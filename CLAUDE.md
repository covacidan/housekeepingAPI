# CLAUDE.md — housekeepingAPI

Active REST API backend for the housekeeping water-index application.

**Stack:** Spring Boot 3.5.3, Java 17, PostgreSQL 15, Keycloak OAuth2, Docker

---

## Commands

```bash
# Run tests (uses H2 in-memory DB via test profile)
./mvnw test -Dspring.profiles.active=test

# Build jar
./mvnw clean package -DskipTests

# Run locally (requires PostgreSQL + Keycloak running)
./mvnw spring-boot:run

# Docker build
docker build -t housekeeping-api:latest .

# Full stack via compose (requires external housekeeping_net)
docker-compose up -d
```

---

## Package Structure

Root package: `housekeeping.tineretului`

| Package | Contents |
|---------|----------|
| `controller` | `IndexApaController` (water index CRUD), `AuthController` (user management) |
| `service` | `IndexApaService` interface + `IndexApaServiceImpl`, `KeycloakAdminService` |
| `model` | `IndexApa` JPA entity — unique constraint on `(year, month)` |
| `repository` | `IndexApaRepository` — Spring Data JPA, custom `findByMonthAndYear()` |
| `dto` | `IndexApaRequest`, `IndexTotal`, `UserRequest`, `UserResponse` |
| `security` | `SecurityConfig` (OAuth2 resource server), `KeycloakRoleConverter` |

---

## API Endpoints

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/housekeeping/waterIndex` | RECORDER+ | List all records |
| `POST` | `/housekeeping/waterIndex` | RECORDER+ | Create new record |
| `PUT` | `/housekeeping/waterIndex/{id}` | ADMIN | Update record |
| `DELETE` | `/housekeeping/waterIndex/{id}` | ADMIN | Delete record |
| `GET` | `/housekeeping/waterIndex/total` | RECORDER+ | Aggregated consumption delta |
| `GET\|POST\|DELETE` | `/auth/users` | ADMIN | Keycloak user management |

---

## Authentication

- **No local auth** — stateless OAuth2 resource server only
- JWT tokens issued by Keycloak (`housekeeping` realm, `housekeeping-ui` client)
- `SecurityConfig` validates tokens against Keycloak's `/certs` endpoint
- `KeycloakRoleConverter` extracts `ADMIN`/`RECORDER` roles from JWT claims
- Tokens passed as `Authorization: Bearer <token>` header

---

## Data Model

**`IndexApa`** — one row per month:
- `year`, `month` — unique composite key
- `receB`, `caldB` — bathroom cold/hot readings
- `receB2`, `caldB2` — service bathroom cold/hot readings
- `receB3`, `caldB3` — kitchen cold/hot readings

---

## Tests

Location: `src/test/java/housekeeping/tineretului/`

- `HousekeepingTests` — context load smoke test
- `IndexApaControllerTest` — controller unit tests (mocked service, no security autoconfiguration)
- `IndexApaServiceImplTest` — service layer unit tests
- `AuthControllerTest` — auth endpoint tests

Test profile activates `application-test.properties` (H2 in-memory DB, no Keycloak required).

---

## Environment Variables

Copy `.env.example` to `.env` before running locally:

```
DB_HOST          PostgreSQL host (default: localhost)
DB_NAME          Database name
DB_USER          Database user
DB_PASSWORD      Database password
KEYCLOAK_ADMIN_USER     Keycloak admin username
KEYCLOAK_ADMIN_PASSWORD Keycloak admin password
VAULT_ADDR       Vault address (Phase 2)
VAULT_ROLE_ID    Vault AppRole ID (Phase 2)
VAULT_SECRET_ID  Vault AppRole secret (Phase 2)
```

In production, secrets are fetched from Vault via the `api` AppRole at runtime (see `housekeepingTF/`).

---

## CI/CD — GitHub Actions

Workflow: [`.github/workflows/ci.yml`](.github/workflows/ci.yml)

Triggers on push/PR to `main`.

Steps: Checkout → setup Java 17 → `./mvnw test -Dspring.profiles.active=test` → SonarCloud analysis

**Required GitHub repository config:**
- Secret: `SONAR_TOKEN`
- Variables: `SONAR_ORGANIZATION`, `SONAR_PROJECT_KEY`

**Deploy manually** after CI passes: `docker compose up -d --build` from the repo root.
