# Play MDSG on Mac

MDSG runs on your Mac with **Docker Desktop**. You host the game; friends join in a browser with your invite link (same Wi‑Fi).

## 1. Install Docker Desktop

Download and install: https://www.docker.com/products/docker-desktop/

Open **Docker Desktop** and wait until it shows **Running** (whale icon in the menu bar).

## 2. Get the game

Clone or download this repo, then open the folder in Finder.

## 3. Start the game

Double-click:

```text
scripts/Start MDSG.command
```

(Finder may ask to allow the script the first time — confirm.)

Your browser opens the game. First start can take a few minutes while images build.

## 4. Stop the game

Double-click:

```text
scripts/Stop MDSG.command
```

Or in Terminal from the project folder: `docker compose down`

## Multiplayer with friends

1. Start MDSG (step 3). The start window prints a **Friends (same Wi‑Fi)** URL like `http://192.168.1.42:5173`.
2. On this Mac, use that URL in the browser (not only `localhost`) so invite links work for others.
3. Create a **Multiplayer** game and copy the invite link.
4. Friends on the **same network** open the link in Chrome/Safari — they do **not** need to install anything.

Friends on a different home/network cannot use `localhost` links; they need your LAN URL or an internet tunnel (not included in v1).

## Solo mode

Use the **Solo** tab. Edit puzzles in:

```text
mdsg-client/static/solo-puzzles.json
```

Restart MDSG after changing that file (`Stop` then `Start`).

## Troubleshooting

| Problem | Fix |
|---------|-----|
| “Docker is not running” | Open Docker Desktop, wait until Running |
| “cannot start a paused container” | Run `docker compose down`, then Start MDSG again |
| Friend cannot join | Same Wi‑Fi? Host used LAN URL? Firewall blocking ports 5173/8080? |
| Wikidata slow | Normal — data loads from the internet |

## Terminal alternative

```bash
cd /path/to/imdb-game
docker compose up --build
```

Open http://localhost:5173 (or your LAN IP on port 5173 for friends).
