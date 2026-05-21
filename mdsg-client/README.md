# MDSG Client

SvelteKit UI for **MDSG** (Movie Database Search Game).

## Prerequisites

- Node.js 20+
- Backend running at http://localhost:8080
- Redis running

## Setup

From the repo root:

```bash
cd mdsg-client
cp .env.example .env
npm install
npm run dev
```

Open http://localhost:5173

Start Redis first: `docker compose up -d` from the repo root.

## Play flow

1. **Create** a game on the home page (host).
2. **Copy invite link** on the play screen and open it in another browser/tab (or share with a friend).
3. **Join** as the second player.
4. Take turns clicking **film** / **actor** links from Wikidata until someone reaches the target movie.

## Environment

Copy `.env` if needed:

```
VITE_API_BASE=http://localhost:8080
VITE_WS_URL=http://localhost:8080/ws
```
