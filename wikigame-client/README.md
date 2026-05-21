# Wikigame Client

SvelteKit UI for the movie/actor Wikigame backend.

## Prerequisites

- Node.js 20+ (`brew install node`)
- Backend running at http://localhost:8080
- Redis running

## Setup

From the repo root:

```bash
cd wikigame-client
cp .env.example .env   # optional; defaults match localhost
npm install
npm run dev
```

Start Redis first: `docker compose up -d` from the repo root.

Open http://localhost:5173

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
