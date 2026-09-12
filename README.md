# Hockey Assist — Backend

A distributed NBA analytics platform built with Spring Boot, Apache Kafka, Redis, and PostgreSQL. It ingests player statistics, advanced metrics, and shot chart data from NBA.com, processes them through an event-driven pipeline, and serves them via a REST API.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Data Pipeline](#data-pipeline)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Caching Strategy](#caching-strategy)
- [Getting Started](#getting-started)
- [Environment Configuration](#environment-configuration)
- [Running the Application](#running-the-application)
- [Development Notes](#development-notes)
- [Known Issues](#known-issues)
- [License](#license)

---

## Overview

Hockey Assist is a backend service that provides comprehensive NBA analytics through a REST API. It supports:

- Player profiles with team relationships and headshot URLs
- Season-by-season statistics for 500+ active players
- Advanced metrics (Usage Rate, True Shooting %, Assist Rate, Rebound Rate, Effective FG%)
- Shot chart data for spatial analysis and efficiency visualization
- Career totals, per-game averages, and league leaders
- Player comparison and two-way archetype analysis
- Redis-backed caching for frequently accessed endpoints

The system is designed around an event-driven architecture using Apache Kafka to decouple data ingestion from processing, allowing the pipeline to scale horizontally and handle failures gracefully.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          HOCKEY ASSIST BACKEND                          │
│                                                                         │
│  ┌──────────────┐    ┌─────────────┐    ┌──────────────────────────┐    │
│  │   Python     │    │   Kafka     │    │   Spring Boot            │    │
│  │   Fetchers   │───▶│   Topics    │───▶│   Consumers              │    │
│  │  (3 scripts) │    │             │    │  (3 consumers)           │    │
│  └──────────────┘    └─────────────┘    └──────────┬───────────────┘    │
│                                                     │                   │
│                                                     ▼                   │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                     PostgreSQL                                   │   │
│  │  players │ teams │ player_season_stats │ player_season_averages  │   │
│  │  player_season_advanced │ player_shots │ player_headshots        │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                     │                   │
│                                                     ▼                   │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                       Redis Cache                                │   │
│  │  players (30m) │ seasons (15m) │ leaders (5m) │ advanced (30m)   │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                     │                   │
│                                                     ▼                   │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                    REST API (StatsController)                    │   │
│  │                    20+ endpoints under /api/stats                │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 4.1 |
| Persistence | Spring Data JPA, Hibernate 7.4 |
| Database | PostgreSQL 15 |
| Cache | Redis 7 |
| Message Broker | Apache Kafka 7.6 (with Zookeeper) |
| Data Ingestion | Python 3.10+, nba_api |
| Build Tool | Maven (wrapper included) |
| Containerization | Docker, Docker Compose |

---

## Project Structure

```
hockeyassist/
├── src/main/java/com/hockeyassist/hockeyassist/
│   ├── HockeyassistApplication.java
│   ├── config/
│   │   ├── KafkaConfig.java
│   │   ├── RedisConfig.java
│   │   └── WebConfig.java
│   ├── controller/
│   │   └── StatsController.java
│   ├── dto/
│   │   ├── PlayerDTO.java
│   │   ├── PlayerSeasonStatsDTO.java
│   │   ├── PlayerSeasonAveragesDTO.java
│   │   ├── PlayerSeasonAdvancedDTO.java
│   │   ├── PlayerShotDTO.java
│   │   └── PlayerVennDTO.java
│   ├── model/
│   │   ├── Player.java
│   │   ├── Team.java
│   │   ├── PlayerSeasonStats.java
│   │   ├── PlayerSeasonAverages.java
│   │   ├── PlayerSeasonAdvanced.java
│   │   ├── PlayerShot.java
│   │   └── PlayerHeadshot.java
│   ├── repository/
│   │   ├── PlayerRepository.java
│   │   ├── TeamRepository.java
│   │   ├── PlayerSeasonStatsRepository.java
│   │   ├── PlayerSeasonAveragesRepository.java
│   │   ├── PlayerSeasonAdvancedRepository.java
│   │   ├── PlayerShotRepository.java
│   │   └── PlayerHeadshotRepository.java
│   └── service/
│       ├── PlayerService.java
│       ├── PlayerSeasonStatsService.java
│       ├── PlayerSeasonAveragesService.java
│       ├── PlayerSeasonAdvancedService.java
│       ├── ShotService.java
│       ├── HeadshotService.java
│       ├── VennDiagramService.java
│       ├── KafkaPlayerConsumerService.java
│       ├── KafkaAdvancedMetricsConsumer.java
│       └── KafkaShotConsumer.java
├── src/main/resources/
│   └── application.properties
├── scripts/python/
│   ├── fetch_player_data.py
│   ├── fetch_advanced_stats.py
│   └── fetch_shot_data.py
├── data/
│   ├── all_players_data.json
│   ├── all_teams_data.json
│   └── ...
├── docker-compose.yml
├── pom.xml
└── mvnw / mvnw.cmd
```

---

## Data Pipeline

The backend consumes from three Kafka topics, each corresponding to a data source:

### 1. `player-stats` — Core Player Data

Produced by `fetch_player_data.py`.

Processed by `KafkaPlayerConsumerService`.

Payload includes:
- Player identity (NBA ID, name, position, team)
- Headshot URL
- Season-by-season statistics (points, rebounds, assists, shooting splits, etc.)

On consumption, the service:
1. Upserts the player record
2. Resolves the team by NBA team ID
3. Upserts season stats with `(player_id, season_id)` uniqueness
4. Triggers averages calculation

### 2. `player_advanced_metrics` — Advanced Metrics

Produced by `fetch_advanced_stats.py`.

Processed by `KafkaAdvancedMetricsConsumer`.

Payload includes:
- Usage Rate (`USG_PCT`)
- True Shooting Percentage (`TS_PCT`)
- Assist Rate (`AST_PCT`)
- Rebound Rate (`REB_PCT`)
- Effective Field Goal Percentage (`EFG_PCT`)
- Games Played (`GP`)

On consumption, the service upserts into `player_season_advanced`.

### 3. `player_shot_data` — Shot Chart Data

Produced by `fetch_shot_data.py`.

Processed by `KafkaShotConsumer`.

Payload includes:
- Court coordinates (`LOC_X`, `LOC_Y`)
- Shot zones (`SHOT_ZONE_BASIC`, `SHOT_ZONE_AREA`, `SHOT_ZONE_RANGE`)
- Made/missed flag
- Distance, game ID, game event ID

On consumption, the service checks idempotency via `(game_id, game_event_id)` before inserting.

---

## Database Schema

Seven tables back the API:

| Table | Purpose | Key Relationships |
|-------|---------|-------------------|
| `players` | Player identity and current team | `team_id` → `teams.team_id` |
| `teams` | 30 NBA teams with metadata | Primary key on `team_id` |
| `player_season_stats` | Raw season totals | `player_id` → `players.id` |
| `player_season_averages` | Derived per-game averages | `player_id` → `players.id` |
| `player_season_advanced` | Advanced rate metrics | `player_id` → `players.id` |
| `player_shots` | Individual shot attempts | `player_id` → `players.id` |
| `player_headshots` | Headshot URLs and cached image data | `player_id` (NBA ID) |

### Unique Constraints

- `players.nba_player_id` — prevents duplicate players
- `player_season_stats (player_id, season_id)` — one stat row per player per season
- `player_season_averages (player_id, season_id)` — one averages row per player per season
- `player_season_advanced (player_id, season_id)` — one advanced row per player per season
- `player_shots (game_id, game_event_id)` — prevents duplicate shot inserts

---

## API Endpoints

All endpoints are prefixed with `/api/stats`.

### Players

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/players` | All players (paginated) |
| GET | `/players/{nbaPlayerId}` | Single player by NBA ID |
| GET | `/players/search?query=` | Search by name |
| GET | `/players/team/{team}` | Players on a team |
| GET | `/players/position/{position}` | Players by position |
| GET | `/players/active` | All active players |

### Season Stats

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/players/{id}/seasons` | All seasons for a player |
| GET | `/players/{id}/seasons/{season}` | Specific season |
| GET | `/players/{id}/career` | Career totals |
| GET | `/players/{id}/averages` | Career per-game averages |
| GET | `/players/{id}/best/{stat}` | Best season for a stat |

### Advanced Metrics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/players/{id}/advanced/seasons` | Advanced metrics by season |

### Averages

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/players/{id}/averages/seasons` | Per-game averages by season |

### Shots

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/players/{id}/shots?season=` | Shot chart data |
| GET | `/players/{id}/shots/seasons` | Seasons with shot data |

### Headshots

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/players/{id}/headshot` | Headshot URL |
| GET | `/players/{id}/headshot-image` | Cached image bytes |

### Leaders

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/leaders/{season}/{stat}` | League leaders by stat |
| GET | `/leaders/all-time/{stat}` | All-time leaders |

### Teams

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/teams` | All team abbreviations |
| GET | `/teams/{team}/players` | Players on a team |
| GET | `/teams/{team}/averages/{season}` | Team averages |

### Season Analytics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/seasons` | All available seasons |
| GET | `/seasons/{season}/players` | All players in a season |

### Comparison and Archetypes

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/compare?player1=&player2=` | Compare two players |
| GET | `/players/venn/two-way?season=` | Two-way archetype distribution |

---

## Caching Strategy

Redis caches frequently requested data with configurable TTLs:

| Cache | TTL | Invalidated By |
|-------|-----|----------------|
| `players` | 30 min | Player save/delete |
| `seasons` | 15 min | — |
| `leaders` | 5 min | — |
| `advanced` | 30 min | — |
| `averages` | 30 min | — |

The Redis value serializer uses type information so cached DTOs reconstruct correctly on read. Entities are never cached directly — only DTOs.

---

## Getting Started

### Prerequisites

- Java 17
- Docker and Docker Compose
- Python 3.10+ (for data ingestion)
- Maven (or use the included wrapper)

### Clone the Repository

```bash
git clone https://github.com/yourusername/hockeyassist.git
cd hockeyassist
```

### Start Infrastructure

```bash
docker-compose up -d
```

This starts four containers:

| Container | Port | Purpose |
|-----------|------|---------|
| `postgres-hockey` | 5433 | Database |
| `redis` | 6379 | Cache |
| `kafka` | 9092 | Message broker |
| `zookeeper` | 2181 | Kafka coordination |

Verify all four are running:

```bash
docker ps
```

---

## Environment Configuration

`src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5433/hockeyassist
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=2000ms

# Logging
logging.level.org.springframework=INFO
logging.level.org.hibernate=INFO
```

---

## Running the Application

### 1. Start Spring Boot

```bash
./mvnw spring-boot:run
```

The application listens on `http://localhost:8080`.

### 2. Ingest Data

Run the Python scripts in order. Each publishes messages to Kafka; the Spring Boot consumers persist them.

```bash
cd scripts/python

# Core player data and season stats
python fetch_player_data.py

# Advanced metrics (USG%, TS%, AST%, REB%, EFG%)
python fetch_advanced_stats.py

# Shot chart data
python fetch_shot_data.py
```

The scripts use thread-local Kafka producers and batch publishing to handle millions of records efficiently. Expect the full pipeline (530+ players, 5 seasons, all three datasets) to take 90–180 minutes depending on NBA API response times.

### 3. Verify

```bash
# Check a player
curl http://localhost:8080/api/stats/players/2544 | jq

# Check advanced metrics
curl http://localhost:8080/api/stats/players/2544/advanced/seasons | jq

# Check shots
curl "http://localhost:8080/api/stats/players/2544/shots?season=2025-26" | jq '.[0]'
```

---

## Development Notes

### Architecture Decisions

- **Event-driven ingestion.** Decoupling Python fetchers from Java consumers means either side can fail without losing data. Kafka retains messages for the configured retention period.
- **Two-layer deduplication.** The consumers check for existing records before inserting, and the database enforces unique constraints as a safety net.
- **DTOs everywhere.** Entities are never serialized directly to JSON or cached in Redis. This prevents Hibernate lazy-loading proxies from leaking and keeps the API contract stable across schema changes.
- **Thread-local Kafka producers.** The Python scripts use one producer per worker thread because `kafka-python` is not thread-safe for concurrent sends.

### Testing

```bash
./mvnw test
```

### Building a JAR

```bash
./mvnw clean package
java -jar target/hockeyassist-0.0.1-SNAPSHOT.jar
```

### Common Commands

| Task | Command |
|------|---------|
| Rebuild and run | `./mvnw clean compile spring-boot:run` |
| Restart a container | `docker restart kafka` |
| Clear Redis cache | `docker exec -it redis redis-cli FLUSHALL` |
| List Kafka topics | `docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list` |
| Peek at a topic | `docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic player_shot_data --max-messages 1` |
| Connect to PostgreSQL | `docker exec -it postgres-hockey psql -U postgres -d hockeyassist` |
| Count shots | `docker exec -it postgres-hockey psql -U postgres -d hockeyassist -c "SELECT COUNT(*) FROM player_shots;"` |

---

## Known Issues

- **`kafka-python` deprecation warnings.** Serializer parameters do not implement the base `Serializer` class, which triggers warnings on every producer instantiation. Functionally harmless; suppression is possible by wrapping the serializers.
- **NBA API rate limits.** Parallel fetches above ~15 concurrent workers occasionally trigger 429 responses. The retry logic handles this, but throughput drops.
- **`@JsonIgnore` on `Player.team`.** The `team` relation is lazy-loaded and omitted from JSON serialization to avoid Hibernate proxy errors. Team data is exposed through `PlayerDTO.team` and `PlayerDTO.teamName` instead.

---

## License

This project is for educational and portfolio purposes. NBA data is fetched from NBA.com via the open-source `nba_api` package. All trademarks belong to their respective owners.