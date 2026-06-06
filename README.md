<p align="center">
  <img alt="Screenshot" src="assets/banner.svg" width="300"/>
</p>

<p align="center">
  <!-- REPLACE: Update badges with your actual repository URLs -->
  <a href="https://github.com/golmenero/ratelog">
  <img alt="License" src="https://img.shields.io/github/license/golmenero/ratelog.svg"/>
  </a>
  <a href="https://github.com/golmenero/ratelog/releases">
  <img alt="Current Release" src="https://img.shields.io/github/release/golmenero/ratelog.svg"/>
  </a>
  <a href="https://github.com/golmenero/ratelog/actions/workflows/ci.yml">
  <img alt="CI Status" src="https://img.shields.io/github/actions/workflow/status/golmenero/ratelog/ci.yml.svg"/>
  </a>
  <a href="https://hub.docker.com/r/golmenero/ratelog">
  <img alt="Docker Pulls" src="https://img.shields.io/docker/pulls/golmenero/ratelog.svg"/>
  </a>
  <a href="https://www.buymeacoffee.com/golmenero">
  <img alt="Buy Me a Coffee" src="https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?logo=buymeacoffee&logoColor=black"/>
  </a>


  <br/>
</p>

---

Ratelog is a web application that lets you search for movies and TV shows on TMDB, rate them across multiple categories, and generate ranked top lists. It supports multi-user accounts with authentication, a dark-themed responsive UI, and follows/tracking for upcoming releases.

No premium tiers, no hidden features — just a straightforward tool to track and rank what you watch.

<strong>Want to get started?</strong><br/>
Check out the <a href="#run-locally">installation guide</a> or jump straight to <a href="#docker">Docker deployment</a>.<br/>

<strong>Something not working right?</strong><br/>
Open an <a href="https://github.com/golmenero/ratelog/issues">Issue</a> on GitHub.<br/>

<strong>Want to contribute?</strong><br/>
Check out the contributing guide (coming soon).<br/>

<strong>New idea or improvement?</strong><br/>
Open a <a href="https://github.com/golmenero/ratelog/discussions">Discussion</a> on GitHub.<br/>
---

## Features

- **Search** — Find movies and TV shows via TMDB's API
- **Follow** — Track upcoming releases and see them on the premieres page (grouped by Released / Upcoming / No Date)
- **Rate by category** — Score each title from 1 to 10 (0.25 steps) across 5 categories:
  - Directing
  - Cinematography
  - Acting
  - Soundtrack
  - Screenplay
- **Average score** — Automatic mean calculation across all 5 categories
- **Top lists** — Separate pages for movies and TV shows, filterable by year and category with configurable limits
- **Multi-user** — Each user has their own ratings, follows, and tops
- **One rating per title** — Delete and re-rate if you change your mind

<!-- REPLACE: Add screenshots or demo images here -->
<p align="center">
  <img alt="Search" src="assets/search.png" width="900"/>
  <br>
  <img alt="Detail" src="assets/detail.png" width="900"/>
  <br>
  <img alt="Rate" src="assets/rate.png" width="900"/>
  <br>
  <img alt="Top" src="assets/top.png" width="900"/>
</p>

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Kotlin 2.3.10 + Spring Boot 3.4.5 |
| **Database** | PostgreSQL 17 (Flyway migrations) |
| **Frontend** | Thymeleaf server-rendered HTML + CSS (dark theme, responsive) |
| **Auth** | Spring Security (BCrypt, form login) |

---

## Run Locally

### Prerequisites

- JDK 21
- PostgreSQL running locally (or adjust connection variables)
- TMDB API key ([get one here](https://www.themoviedb.org/settings/api))

### Steps

```powershell
# 1. Set environment variables
$env:TMDB_API_KEY="YOUR_API_KEY"

# 2. Run
mvn clean compile exec:java
```

Open in browser: `http://localhost:8080/`

---

## Docker

### Build Image

```powershell
docker build -t ratelog .
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

| Service | Description |
|---|---|
| **postgres** | PostgreSQL 17 (port 5432, volume `pgdata`) |
| **ratelog** | App (port 8080, depends on healthy postgres) |

---

## Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `TMDB_API_KEY` | Yes | — | TMDB API key |
| `PORT` | No | `8080` | HTTP port |
| `POSTGRES_HOST` | No | `localhost` | PostgreSQL host |
| `POSTGRES_PORT` | No | `5432` | PostgreSQL port |
| `POSTGRES_DB` | No | `ratelog` | Database name |
| `POSTGRES_USER` | No | `ratelog` | Database user |
| `POSTGRES_PASSWORD` | No | `ratelog` | Database password |

---

## Deploy to TrueNAS SCALE

Minimal flow:

1. Copy `.env.example` to `.env` in your Truenas Custom App stack
2. Set `TMDB_API_KEY` and `POSTGRES_DATA_DIR` (path `/mnt/<pool>/...`)
3. Set `RATLOG_IMAGE=ghcr.io/<owner>/ratelog:latest` (or specific version)
4. Deploy with compose
5. Verify at `http://IP_TRUENAS:8080/api/health`

---

<p align="center">
This project is powered by:
<br/>
<br/>
<!-- REPLACE: Add sponsor/partner logos here -->
<a href="https://www.themoviedb.org/documentation/api">
<img alt="TMDB API" src="assets/tmdb.svg" height="20px"/>
</a>
&nbsp;&nbsp;&nbsp;
<a href="https://spring.io/projects/spring-boot">
<img alt="Spring Boot" src="assets/spring.svg" height="20px"/>
</a>
&nbsp;&nbsp;&nbsp;
<a href="https://www.postgresql.org/">
<img alt="PostgreSQL" src="assets/postgres.svg" height="20px"/>
</a>
</p>
