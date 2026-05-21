# MDSG — Movie Database Search Game

Two-player browser game: hop from **film → actor → film** on [Wikidata](https://www.wikidata.org/) until you reach the target movie. Inspired by the “Six Degrees” idea, using live Wikidata graph data—not IMDb.

## Stack

| Layer | Tech |
|-------|------|
| Frontend | SvelteKit 2, Svelte 5, TypeScript, STOMP over SockJS |
| Backend | Spring Boot 3.5, Java 17 |
| State | Redis (game sessions, TTL) |
| Data | Wikidata SPARQL + Action API |
| Realtime | WebSocket broadcasts per game |

## Quick start (Docker — recommended)

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

```bash
git clone https://github.com/christhentopher-cpu/wikigame.git
cd wikigame
docker compose up --build
```

First run builds images (a few minutes). Then open **http://localhost:5173**.

Stop with `Ctrl+C` or `docker compose down`.

## Quick start (local dev)

**Prerequisites:** Node.js 20+, Java 17+, Redis

```bash
# Redis (pick one)
docker compose up -d redis
# brew install redis && brew services start redis

# Backend (port 8080)
cd mdsg-server && ./mvnw spring-boot:run

# Frontend (port 5173) — new terminal
cd mdsg-client
cp .env.example .env
npm install && npm run dev
```

Open http://localhost:5173

## How to play

1. **Host** creates a game with a start movie and target movie (search uses Wikidata).
2. **Copy the invite link** and open it in another browser or tab.
3. **Guest joins** and plays round 1 on the host’s picks.
4. **Round 2:** guest picks new start/target films; host navigates.
5. Take turns clicking only **valid neighbors** (films cast an actor; actors appear in films) until someone lands on the target movie.

## Project layout

```
mdsg-client/   SvelteKit UI
mdsg-server/   Spring Boot API + WebSocket + Wikidata integration
```

## Development

```bash
# Server tests
cd mdsg-server && ./mvnw test

# Client typecheck
cd mdsg-client && npm run check
```

CI runs both on every push (see `.github/workflows/ci.yml`).

## Data & attribution

Film and actor data come from **Wikidata**. This project is not affiliated with Wikimedia or IMDb. When deploying, set a descriptive `mdsg.wikidata.user-agent` in `application.yml` per the [Wikimedia User-Agent policy](https://meta.wikimedia.org/wiki/User-Agent_policy).

## License

MIT — see [LICENSE](LICENSE).
