package com.wikigame.domain;

import java.time.Instant;

public record GameState(
		String gameId,
		GamePhase phase,
		int round,
		WikidataNode startMovie,
		WikidataNode currentNode,
		WikidataNode targetMovie,
		/** Player who may click links or submit round-two setup; the other player watches. */
		String activePlayerId,
		int clickCount,
		GamePlayer playerOne,
		GamePlayer playerTwo,
		Instant createdAt) {
}
