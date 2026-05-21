package com.wikigame.ws;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.wikigame.api.MoveRequest;
import com.wikigame.game.GameService;

import jakarta.validation.Valid;

@Controller
public class GameWsController {

	private final GameService gameService;

	public GameWsController(GameService gameService) {
		this.gameService = gameService;
	}

	@MessageMapping("/game/{gameId}/move")
	public void move(@DestinationVariable String gameId, @Valid @Payload MoveRequest request) {
		gameService.applyMove(gameId, request);
	}

}
