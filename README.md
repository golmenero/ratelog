# Raterr

Web app to search for movies and TV shows on TMDB, rate them by categories, and generate ranked top lists. Supports multi-user accounts with authentication.

## Stack
- **Backend:** Kotlin 2.3.10 + Spring Boot 3.4.5
- **DB:** PostgreSQL 17 (with Flyway migrations)
- **Frontend:** Thymeleaf server-rendered HTML + CSS (dark theme, responsive)
- **Auth:** Spring Security (BCrypt, form login)
- **Caching:** Caffeine (in-memory, TMDB responses)

## Features
- Search movies and TV shows on TMDB
- Follow content to track upcoming releases
- Premieres page: followed content grouped by Released / Upcoming / No Date
- Rating by categories (1-10, step 0.25):
  - Directing
  - Cinematography
  - Acting
  - Soundtrack
  - Screenplay
- Average score calculation per title (mean of all 5 categories)
- One rating per user per title (delete first to re-rate)
- Tops:
  - Movies and TV shows (separate pages with integrated premieres)
  - Filterable by year and category
  - Configurable limit

## Data Model
- `users` — user accounts (username, email, password hash)
- `movies` — TMDB movie metadata
- `tv_shows` — TMDB TV show metadata
- `ratings` — movie ratings per user per category
- `tv_ratings` — TV show ratings per user per category
- `follows` — user follows (content type + TMDB ID)

## Environment Variables
| Variable | Required | Default | Description |
|---|---|---|---|
| `TMDB_API_KEY` | Yes | — | TMDB API key |
| `PORT` | No | `8080` | HTTP port |
| `POSTGRES_HOST` | No | `localhost` | PostgreSQL host |
| `POSTGRES_PORT` | No | `5432` | PostgreSQL port |
| `POSTGRES_DB` | No | `raterr` | Database name |
| `POSTGRES_USER` | No | `raterr` | Database user |
| `POSTGRES_PASSWORD` | No | `raterr` | Database password |

## Run Locally

### Prerequisites
- JDK 21
- PostgreSQL running locally (or adjust connection vars)

### Steps
```powershell
# 1. Create .env or set variables
$env:TMDB_API_KEY="YOUR_API_KEY"

# 2. Run
mvn clean compile exec:java
```

Open in browser:
- `http://localhost:8080/`
## Docker

### Build Image
```powershell
docker build -t raterr .
```

### Run with Docker Compose
Includes PostgreSQL service:
```powershell
# Option A: variables in session
$env:TMDB_API_KEY="YOUR_API_KEY"
docker compose up --build

# Option B: .env file in project root (copy from .env.example)
docker compose up --build
```

Compose services:
- **postgres** — PostgreSQL 17 (port 5432, volume `pgdata`)
- **raterr** — App (port 8080, depends on healthy postgres)

## Endpoints

### Pages (auth required unless noted)
| Method | Path | Description |
|---|---|---|
| `GET` | `/` | Search movies & TV shows |
| `GET` | `/login` | Login page (public) |
| `POST` | `/login/process` | Login form processing (public) |
| `GET` | `/register` | Registration page (public) |
| `POST` | `/register` | Create account (public) |
| `POST` | `/logout` | Logout |
| `GET` | `/movie/rate?id=X` | Movie rating page |
| `POST` | `/movie/rate` | Submit movie rating |
| `POST` | `/movies/delete/{id}` | Delete movie rating |
| `GET` | `/movies` | Top movies + premieres (query: `limit`, `year`, `category`) |
| `GET` | `/tv/rate?id=X` | TV show rating page |
| `POST` | `/tv/rate` | Submit TV show rating |
| `POST` | `/tvshows/delete/{id}` | Delete TV show rating |
| `GET` | `/tvshows` | Top TV shows + premieres (query: `limit`, `year`, `category`) |
| `POST` | `/follow` | Toggle follow/unfollow (params: `tmdbId`, `type`, `q`) |

### API
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/health` | Health check — `{"status":"ok"}` (public) |

## CI/CD

### GitHub Actions
- **`ci.yml`** — Runs on push/PR to `master`: tests, package, Docker build
- **`release.yml`** — Runs on tag `v*` or manual dispatch: builds and pushes to GHCR

### Publish Image to GHCR
```powershell
git tag v1.0.0
git push origin v1.0.0
```

Produces:
- `ghcr.io/<owner>/raterr:vX.Y.Z`
- `ghcr.io/<owner>/raterr:latest`

## Deploy to TrueNAS SCALE (Dockge)

Files in `deploy/dockge/`:
- `.env.example` — Stack variables template

Minimal flow:
1. Copy `deploy/dockge/.env.example` to `.env` in your Dockge stack
2. Set `TMDB_API_KEY` and `POSTGRES_DATA_DIR` (path `/mnt/<pool>/...`)
3. Set `RATERR_IMAGE=ghcr.io/<owner>/raterr:latest` (or specific version)
4. Deploy with compose
5. Verify at `http://IP_TRUENAS:8080/api/health`
