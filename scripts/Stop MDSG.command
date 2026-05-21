#!/bin/bash
cd "$(dirname "$0")/.." || exit 1

echo "Stopping MDSG…"
docker compose down

echo "MDSG stopped."
read -n 1 -s -r -p "Press Return to close…"
