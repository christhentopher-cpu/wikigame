package com.wikigame.api;

import com.wikigame.config.GameMessaging;
import com.wikigame.domain.GameState;

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
