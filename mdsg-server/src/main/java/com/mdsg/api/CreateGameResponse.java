package com.mdsg.api;

import com.mdsg.config.GameMessaging;
import com.mdsg.domain.GameState;

public record CreateGameResponse(
		String gameId,
		String playPath,
		String websocketTopic,
		GameState state) {

	public static CreateGameResponse from(GameState state) {
		return new CreateGameResponse(
				state.gameId(),
				"/play/" + state.gameId(),
				GameMessaging.gameTopic(state.gameId()),
				state);
	}

}
