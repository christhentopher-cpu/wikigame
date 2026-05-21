package com.mdsg.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mdsg.domain.GameState;
import com.mdsg.game.GameService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/games")
public class GameController {

	private final GameService gameService;

	public GameController(GameService gameService) {
		this.gameService = gameService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CreateGameResponse createGame(@Valid @RequestBody CreateGameRequest request) {
		GameState state = gameService.createGame(request);
		return CreateGameResponse.from(state);
	}

	@PostMapping("/solo")
	@ResponseStatus(HttpStatus.CREATED)
	public CreateGameResponse createSoloGame(@Valid @RequestBody CreateGameRequest request) {
		GameState state = gameService.createSoloGame(request);
		return CreateGameResponse.from(state);
	}

	@PostMapping("/{gameId}/join")
	public JoinGameResponse joinGame(@PathVariable String gameId, @Valid @RequestBody JoinGameRequest request) {
		GameState state = gameService.joinGame(gameId, request);
		String playerId = state.playerTwo().id();
		return JoinGameResponse.from(state, playerId);
	}

	@GetMapping("/{gameId}/neighbors")
	public NeighborsResponse getNeighbors(
			@PathVariable String gameId,
			@RequestParam(name = "nodeId", required = false) String nodeId) {
		return gameService.getNeighbors(gameId, nodeId);
	}

	@PostMapping("/{gameId}/round-setup")
	public GameState configureRoundTwo(
			@PathVariable String gameId,
			@Valid @RequestBody RoundSetupRequest request) {
		return gameService.configureRoundTwo(gameId, request);
	}

	@PostMapping("/{gameId}/give-up")
	public GameState giveUp(@PathVariable String gameId, @Valid @RequestBody GiveUpRequest request) {
		return gameService.giveUp(gameId, request);
	}

	@PostMapping("/{gameId}/end")
	public GameState endMatch(@PathVariable String gameId, @Valid @RequestBody GiveUpRequest request) {
		return gameService.endMatch(gameId, request);
	}

	@PostMapping("/{gameId}/move")
	public GameState move(@PathVariable String gameId, @Valid @RequestBody MoveRequest request) {
		return gameService.applyMove(gameId, request);
	}

	@GetMapping("/{gameId}")
	public GameState getGame(@PathVariable String gameId) {
		return gameService.getGame(gameId);
	}

}
