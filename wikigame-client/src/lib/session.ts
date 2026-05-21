import type { PlayerSlot } from './types';

export interface PlayerSession {
	playerId: string;
	displayName: string;
	slot: PlayerSlot;
}

function storageKey(gameId: string) {
	return `wikigame:player:${gameId}`;
}

export function getPlayerSession(gameId: string): PlayerSession | null {
	if (typeof sessionStorage === 'undefined') {
		return null;
	}
	const raw = sessionStorage.getItem(storageKey(gameId));
	if (!raw) {
		return null;
	}
	try {
		return JSON.parse(raw) as PlayerSession;
	} catch {
		return null;
	}
}

export function setPlayerSession(gameId: string, session: PlayerSession) {
	sessionStorage.setItem(storageKey(gameId), JSON.stringify(session));
}

export function clearPlayerSession(gameId: string) {
	sessionStorage.removeItem(storageKey(gameId));
}
