export type NodeType = 'ACTOR' | 'FILM';
export type GamePhase =
	| 'WAITING_FOR_OPPONENT'
	| 'SOLO_PLAY'
	| 'ROUND_ONE_PLAY'
	| 'ROUND_TWO_SETUP'
	| 'ROUND_TWO_PLAY'
	| 'FINISHED';
export type PlayerSlot = 'ONE' | 'TWO';

export interface WikidataNode {
	id: string;
	label: string;
	type: NodeType;
}

export interface GamePlayer {
	id: string;
	displayName: string;
	slot: PlayerSlot;
}

export interface RoundSetupRequest {
	playerId: string;
	startMovieId: string;
	targetMovieId: string;
}

export interface GameState {
	gameId: string;
	phase: GamePhase;
	round: number;
	startMovie: WikidataNode;
	currentNode: WikidataNode;
	targetMovie: WikidataNode;
	activePlayerId: string;
	clickCount: number;
	playerOne: GamePlayer;
	playerTwo: GamePlayer | null;
	createdAt: string;
}

export interface FilmSearchResult {
	id: string;
	label: string;
	description: string;
}

export interface CreateGameRequest {
	startMovieId: string;
	targetMovieId: string;
	hostPlayerName: string;
}

export interface CreateGameResponse {
	gameId: string;
	playPath: string;
	websocketTopic: string;
	state: GameState;
}

export interface JoinGameRequest {
	playerName: string;
}

export interface JoinGameResponse {
	gameId: string;
	playerId: string;
	websocketTopic: string;
	state: GameState;
}

export interface NeighborsResponse {
	gameId: string;
	currentNode: WikidataNode;
	neighbors: WikidataNode[];
}

export interface MoveRequest {
	playerId: string;
	nodeId: string;
}

export interface GiveUpRequest {
	playerId: string;
}

export interface ProblemDetail {
	title?: string;
	detail?: string;
	status?: number;
}
