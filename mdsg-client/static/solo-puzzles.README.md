# Solo puzzles (`solo-puzzles.json`)

Edit `solo-puzzles.json` in this folder to add start/end movie pairs for **Solo** mode.

Each puzzle needs Wikidata film IDs (`Q…`):

```json
{
  "id": "my-puzzle",
  "title": "Short description shown in the list",
  "startMovieId": "Q105598",
  "startMovieLabel": "Die Hard",
  "targetMovieId": "Q25188",
  "targetMovieLabel": "Inception"
}
```

- `startMovieId` / `targetMovieId` — **required** (must be films on Wikidata). Look up IDs on [wikidata.org](https://www.wikidata.org/) — the label in this file is only for display; the **QID must be the actual film**.
- `startMovieLabel` / `targetMovieLabel` — optional; shown in the solo puzzle list before play.
- `id` / `title` — optional; help you organize puzzles.

After saving, refresh the app (or restart `npm run dev`). Solo tab loads this file from `/solo-puzzles.json`.
