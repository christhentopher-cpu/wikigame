package com.mdsg.game;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mdsg.api.CreateGameRequest;
import com.mdsg.api.GiveUpRequest;
import com.mdsg.api.JoinGameRequest;
import com.mdsg.api.MoveRequest;
import com.mdsg.api.NeighborsResponse;
import com.mdsg.api.RoundSetupRequest;
import com.mdsg.config.GameMessaging;
import com.mdsg.config.MdsgProperties;
import com.mdsg.domain.GamePhase;
import com.mdsg.domain.GamePlayer;
import com.mdsg.domain.GameState;
import com.mdsg.domain.PlayerSlot;
import com.mdsg.domain.WikidataNode;
import com.mdsg.wikidata.WikidataService;

@Service
public class GameService {

	private final RedisTemplate<String, GameState> gameRedisTemplate;
	private final MdsgProperties properties;
	private final GameBroadcastService broadcastService;
	private final WikidataService wikidataService;

	public GameService(
			RedisTemplate<String, GameState> gameRedisTemplate,
			MdsgProperties properties,
			GameBroadcastService broadcastService,
			WikidataService wikidataService) {
		this.gameRedisTemplate = gameRedisTemplate;
		this.properties = properties;
		this.broadcastService = broadcastService;
		this.wikidataService = wikidataService;
	}

	public GameState createGame(CreateGameRequest request) {
		validateMoviePair(request.startMovieId(), request.targetMovieId());

		String gameId = UUID.randomUUID().toString();
		String hostPlayerId = UUID.randomUUID().toString();

		WikidataNode startMovie = wikidataService.resolveFilm(request.startMovieId());
		WikidataNode targetMovie = wikidataService.resolveFilm(request.targetMovieId());
		GamePlayer host = new GamePlayer(hostPlayerId, request.hostPlayerName(), PlayerSlot.ONE);

		GameState state = new GameState(
				gameId,
				GamePhase.WAITING_FOR_OPPONENT,
				1,
				startMovie,
				startMovie,
				targetMovie,
				hostPlayerId,
				0,
				host,
				null,
				Instant.now());

		saveState(state);
		return state;
	}

	public GameState joinGame(String gameId, JoinGameRequest request) {
		GameState current = getGame(gameId);

		if (current.phase() != GamePhase.WAITING_FOR_OPPONENT) {
			throw new GameConflictException("Game is not waiting for an opponent");
		}
		if (current.playerTwo() != null) {
			throw new GameConflictException("Game already has two players");
		}

		String guestPlayerId = UUID.randomUUID().toString();
		GamePlayer guest = new GamePlayer(guestPlayerId, request.playerName(), PlayerSlot.TWO);

		// Guest navigates round 1 (puzzle set by host); host watches.
		GameState updated = new GameState(
				current.gameId(),
				GamePhase.ROUND_ONE_PLAY,
				1,
				current.startMovie(),
				current.startMovie(),
				current.targetMovie(),
				guestPlayerId,
				0,
				current.playerOne(),
				guest,
				current.createdAt());

		saveState(updated);
		broadcastService.publishState(updated);
		return updated;
	}

	public GameState configureRoundTwo(String gameId, RoundSetupRequest request) {
		GameState game = getGame(gameId);

		if (game.phase() != GamePhase.ROUND_TWO_SETUP) {
			throw new InvalidMoveException("Game is not waiting for round two setup");
		}
		requireNavigator(game, request.playerId());

		validateMoviePair(request.startMovieId(), request.targetMovieId());

		WikidataNode startMovie = wikidataService.resolveFilm(request.startMovieId());
		WikidataNode targetMovie = wikidataService.resolveFilm(request.targetMovieId());

		// Host navigates round 2 (puzzle set by guest); guest watches.
		GameState updated = new GameState(
				game.gameId(),
				GamePhase.ROUND_TWO_PLAY,
				2,
				startMovie,
				startMovie,
				targetMovie,
				game.playerOne().id(),
				0,
				game.playerOne(),
				game.playerTwo(),
				game.createdAt());

		saveState(updated);
		broadcastService.publishState(updated);
		return updated;
	}

	public GameState getGame(String gameId) {
		GameState state = gameRedisTemplate.opsForValue().get(GameMessaging.redisKey(gameId));
		if (state == null) {
			throw new GameNotFoundException(gameId);
		}
		return state;
	}

	public NeighborsResponse getNeighbors(String gameId, String expectedNodeId) {
		GameState game = getGame(gameId);
		if (!isPlayPhase(game.phase())) {
			throw new InvalidMoveException("Game is not in a play phase");
		}
		if (expectedNodeId != null && !expectedNodeId.equals(game.currentNode().id())) {
			throw new GameConflictException(
					"Game position changed — refresh neighbors for " + game.currentNode().id());
		}
		return new NeighborsResponse(
				gameId,
				game.currentNode(),
				wikidataService.getNeighbors(game.currentNode()));
	}

	public GameState endMatch(String gameId, GiveUpRequest request) {
		GameState game = getGame(gameId);

		if (game.phase() == GamePhase.FINISHED) {
			throw new InvalidMoveException("Game is finished");
		}
		requireParticipant(game, request.playerId());

		GameState updated = new GameState(
				game.gameId(),
				GamePhase.FINISHED,
				game.round(),
				game.startMovie(),
				game.currentNode(),
				game.targetMovie(),
				game.activePlayerId(),
				game.clickCount(),
				game.playerOne(),
				game.playerTwo(),
				game.createdAt());

		saveState(updated);
		broadcastService.publishState(updated);
		return updated;
	}

	public GameState giveUp(String gameId, GiveUpRequest request) {
		GameState game = getGame(gameId);

		if (game.phase() == GamePhase.FINISHED) {
			throw new InvalidMoveException("Game is finished");
		}
		if (game.phase() == GamePhase.WAITING_FOR_OPPONENT) {
			throw new InvalidMoveException("Game has not started yet");
		}
		requireParticipant(game, request.playerId());

		GamePhase phase;
		int round = game.round();
		String activePlayerId;

		if (game.phase() == GamePhase.ROUND_ONE_PLAY) {
			// Round 1 over — whoever gives up chooses round 2 movies for the opponent.
			phase = GamePhase.ROUND_TWO_SETUP;
			round = 2;
			activePlayerId = request.playerId();
		}
		else if (game.phase() == GamePhase.ROUND_TWO_SETUP || game.phase() == GamePhase.ROUND_TWO_PLAY) {
			phase = GamePhase.FINISHED;
			activePlayerId = request.playerId();
		}
		else {
			throw new InvalidMoveException("Cannot give up in phase " + game.phase());
		}

		GameState updated = new GameState(
				game.gameId(),
				phase,
				round,
				game.startMovie(),
				game.currentNode(),
				game.targetMovie(),
				activePlayerId,
				game.clickCount(),
				game.playerOne(),
				game.playerTwo(),
				game.createdAt());

		saveState(updated);
		broadcastService.publishState(updated);
		return updated;
	}

	public GameState applyMove(String gameId, MoveRequest request) {
		GameState game = getGame(gameId);

		if (game.phase() == GamePhase.FINISHED) {
			throw new InvalidMoveException("Game is finished");
		}
		if (!isPlayPhase(game.phase())) {
			throw new InvalidMoveException("Game is not in a play phase");
		}
		requireNavigator(game, request.playerId());

		WikidataNode target = wikidataService.resolveNode(request.nodeId());
		wikidataService.requireAlternatingType(game.currentNode().type(), target.type());

		if (!wikidataService.areAdjacent(game.currentNode().id(), target.id())) {
			throw new InvalidMoveException("No cast or filmography link between current node and selection");
		}

		int clickCount = game.clickCount() + 1;
		boolean reachedTarget = target.id().equals(game.targetMovie().id());

		GamePhase phase = game.phase();
		int round = game.round();
		String activePlayerId = game.activePlayerId();

		if (reachedTarget) {
			if (game.phase() == GamePhase.ROUND_ONE_PLAY) {
				phase = GamePhase.ROUND_TWO_SETUP;
				round = 2;
				activePlayerId = game.playerTwo().id();
			}
			else {
				phase = GamePhase.FINISHED;
			}
		}

		GameState updated = new GameState(
				game.gameId(),
				phase,
				round,
				game.startMovie(),
				target,
				game.targetMovie(),
				activePlayerId,
				clickCount,
				game.playerOne(),
				game.playerTwo(),
				game.createdAt());

		saveState(updated);
		broadcastService.publishState(updated);
		return updated;
	}

	private static boolean isPlayPhase(GamePhase phase) {
		return phase == GamePhase.ROUND_ONE_PLAY || phase == GamePhase.ROUND_TWO_PLAY;
	}

	private static void requireNavigator(GameState game, String playerId) {
		if (!game.activePlayerId().equals(playerId)) {
			throw new NotActivePlayerException(playerId);
		}
	}

	private static void requireParticipant(GameState game, String playerId) {
		if (game.playerOne().id().equals(playerId)) {
			return;
		}
		if (game.playerTwo() != null && game.playerTwo().id().equals(playerId)) {
			return;
		}
		throw new InvalidMoveException("Player is not in this game");
	}

	private static void validateMoviePair(String startMovieId, String targetMovieId) {
		if (startMovieId.equals(targetMovieId)) {
			throw new InvalidMoveException("Start movie and destination movie must be different");
		}
	}

	private void saveState(GameState state) {
		String key = GameMessaging.redisKey(state.gameId());
		Duration ttl = properties.getGame().getTtl();
		Long remainingMs = gameRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
		if (remainingMs != null && remainingMs > 0) {
			ttl = Duration.ofMillis(remainingMs);
		}
		gameRedisTemplate.opsForValue().set(key, state, ttl);
	}

}
