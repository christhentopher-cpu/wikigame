package com.wikigame.game;

public class NotActivePlayerException extends RuntimeException {

	public NotActivePlayerException(String playerId) {
		super("Not this player's turn: " + playerId);
	}

}
