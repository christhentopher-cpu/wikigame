#!/bin/bash
# Double-click on Mac (or run from Terminal) to start MDSG.
cd "$(dirname "$0")/.." || exit 1

echo "Starting MDSG (Movie Database Search Game)…"

if ! docker info >/dev/null 2>&1; then
	osascript -e 'display alert "Docker is not running" message "Install Docker Desktop for Mac, open it, wait until it says Running, then double-click Start MDSG again." as critical' 2>/dev/null || {
		echo "Install and start Docker Desktop: https://www.docker.com/products/docker-desktop/"
	}
	exit 1
fi

docker compose up -d --build
if [ $? -ne 0 ]; then
	echo "Failed to start containers. Try: docker compose down && docker compose up -d --build"
	exit 1
fi

echo "Waiting for the game to be ready…"
for i in $(seq 1 60); do
	if curl -sf http://localhost:8080/health >/dev/null && curl -sf http://localhost:5173/ >/dev/null; then
		break
	fi
	sleep 2
done

LAN_IP=""
for iface in en0 en1 en2; do
	ip=$(ipconfig getifaddr "$iface" 2>/dev/null)
	if [ -n "$ip" ]; then
		LAN_IP="$ip"
		break
	fi
done

if [ -n "$LAN_IP" ]; then
	PLAY_URL="http://${LAN_IP}:5173"
	echo ""
	echo "MDSG is running."
	echo "  This Mac:     http://localhost:5173"
	echo "  Friends (same Wi‑Fi): ${PLAY_URL}"
	echo "  Copy invite links from the game — they use the URL in your browser."
	echo "  Tip: open ${PLAY_URL} on this Mac so friends can use your invite links."
	echo ""
	open "$PLAY_URL" 2>/dev/null || open "http://localhost:5173"
else
	PLAY_URL="http://localhost:5173"
	echo ""
	echo "MDSG is running at ${PLAY_URL}"
	echo "  (Could not detect LAN IP — friends on other devices may need the same network.)"
	open "$PLAY_URL"
fi

echo ""
echo "To stop the game, double-click Stop MDSG.command or run: docker compose down"
read -n 1 -s -r -p "Press Return to close this window…"
