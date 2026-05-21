package com.wikigame.config;

/**
 * STOMP destination helpers aligned with the Svelte client contract.
 */
public final class GameMessaging {

	private GameMessaging() {
	}

	public static String gameTopic(String gameId) {
		return "/topic/game/" + gameId;
	}

	public static String moveDestination(String gameId) {
		return "/app/game/" + gameId + "/move";
	}

	public static String redisKey(String gameId) {
		return RedisConfig.GAME_KEY_PREFIX + gameId;
	}

}
