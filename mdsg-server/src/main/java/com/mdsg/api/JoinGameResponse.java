package com.mdsg.api;

import com.mdsg.config.GameMessaging;
import com.mdsg.domain.GameState;

public record JoinGameResponse(
		String gameId,
		String playerId,
		String websocketTopic,
		GameState state) {

	public static JoinGameResponse from(GameState state, String playerId) {
		return new JoinGameResponse(
				state.gameId(),
				playerId,
				GameMessaging.gameTopic(state.gameId()),
				state);
	}

}
