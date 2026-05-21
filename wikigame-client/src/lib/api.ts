import { API_BASE } from './config';
import type {
	CreateGameRequest,
	CreateGameResponse,
	FilmSearchResult,
	GameState,
	JoinGameRequest,
	JoinGameResponse,
	GiveUpRequest,
	MoveRequest,
	NeighborsResponse,
	ProblemDetail,
	RoundSetupRequest
} from './types';

export type { FilmSearchResult };

type RequestOptions = RequestInit & { timeoutMs?: number };

async function request<T>(path: string, init?: RequestOptions): Promise<T> {
	const { timeoutMs, ...fetchInit } = init ?? {};
	const controller = timeoutMs ? new AbortController() : undefined;
	const timer =
		controller && timeoutMs
			? setTimeout(() => controller.abort(), timeoutMs)
			: undefined;

	let response: Response;
	try {
		response = await fetch(`${API_BASE}${path}`, {
			headers: { 'Content-Type': 'application/json', ...(fetchInit.headers ?? {}) },
			signal: controller?.signal,
			...fetchInit
		});
	} catch (e) {
		if (e instanceof DOMException && e.name === 'AbortError') {
			throw new Error('Request timed out — Wikidata may be slow. Try again.');
		}
		throw e;
	} finally {
		if (timer) {
			clearTimeout(timer);
		}
	}

	if (!response.ok) {
		let message = response.statusText;
		try {
			const problem = (await response.json()) as ProblemDetail;
			message = problem.detail ?? problem.title ?? message;
		} catch {
			// ignore parse errors
		}
		throw new Error(message);
	}

	if (response.status === 204) {
		return undefined as T;
	}
	return (await response.json()) as T;
}

export function searchFilms(q: string) {
	const params = new URLSearchParams({ q });
	return request<FilmSearchResult[]>(`/api/wikidata/films/search?${params}`);
}

export function createGame(body: CreateGameRequest) {
	return request<CreateGameResponse>('/api/games', {
		method: 'POST',
		body: JSON.stringify(body)
	});
}

export function joinGame(gameId: string, body: JoinGameRequest) {
	return request<JoinGameResponse>(`/api/games/${gameId}/join`, {
		method: 'POST',
		body: JSON.stringify(body)
	});
}

export function getGame(gameId: string) {
	return request<GameState>(`/api/games/${gameId}`);
}

export function getNeighbors(gameId: string, nodeId: string) {
	const params = new URLSearchParams({ nodeId });
	return request<NeighborsResponse>(`/api/games/${gameId}/neighbors?${params}`, {
		timeoutMs: 60_000
	});
}

export function giveUp(gameId: string, body: GiveUpRequest) {
	return request<GameState>(`/api/games/${gameId}/give-up`, {
		method: 'POST',
		body: JSON.stringify(body)
	});
}

export function endMatch(gameId: string, body: GiveUpRequest) {
	return request<GameState>(`/api/games/${gameId}/end`, {
		method: 'POST',
		body: JSON.stringify(body)
	});
}

export function move(gameId: string, body: MoveRequest) {
	return request<GameState>(`/api/games/${gameId}/move`, {
		method: 'POST',
		body: JSON.stringify(body)
	});
}

export function configureRoundTwo(gameId: string, body: RoundSetupRequest) {
	return request<GameState>(`/api/games/${gameId}/round-setup`, {
		method: 'POST',
		body: JSON.stringify(body)
	});
}
