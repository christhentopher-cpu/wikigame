function localServiceBase(port: number): string {
	if (typeof window === 'undefined') {
		return `http://localhost:${port}`;
	}
	const host = window.location.hostname || 'localhost';
	return `${window.location.protocol}//${host}:${port}`;
}

/** When VITE_* are unset, API/WS follow the page host so LAN invite links work on Mac. */
export const API_BASE = import.meta.env.VITE_API_BASE ?? localServiceBase(8080);
export const WS_URL = import.meta.env.VITE_WS_URL ?? `${localServiceBase(8080)}/ws`;
