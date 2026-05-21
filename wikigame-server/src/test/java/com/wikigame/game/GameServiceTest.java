package com.wikigame.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.wikigame.api.GiveUpRequest;
import com.wikigame.api.MoveRequest;
import com.wikigame.api.RoundSetupRequest;
import com.wikigame.config.WikigameProperties;
import com.wikigame.domain.GamePhase;
import com.wikigame.domain.GamePlayer;
import com.wikigame.domain.GameState;
import com.wikigame.domain.NodeType;
import com.wikigame.domain.PlayerSlot;
import com.wikigame.domain.WikidataNode;
import com.wikigame.wikidata.WikidataService;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

	@Mock
	private RedisTemplate<String, GameState> gameRedisTemplate;

	@Mock
	private ValueOperations<String, GameState> valueOperations;

	@Mock
	private GameBroadcastService broadcastService;

	@Mock
	private WikidataService wikidataService;

	private GameService gameService;

	@BeforeEach
	void setUp() {
		WikigameProperties properties = new WikigameProperties();
		properties.getGame().setTtl(Duration.ofHours(24));
		when(gameRedisTemplate.opsForValue()).thenReturn(valueOperations);

		gameService = new GameService(gameRedisTemplate, properties, broadcastService, wikidataService);
	}

	@Test
	void applyMoveKeepsNavigatorUntilRoundEnds() {
		GameState game = roundOnePlayOnMovie();
		when(valueOperations.get(any())).thenReturn(game);

		WikidataNode brad = new WikidataNode("Q35332", "Brad Pitt", NodeType.ACTOR);
		when(wikidataService.resolveNode("Q35332")).thenReturn(brad);
		when(wikidataService.areAdjacent("Q190908", "Q35332")).thenReturn(true);

		GameState updated = gameService.applyMove(
				"game-123",
				new MoveRequest("player-2", "Q35332"));

		assertThat(updated.phase()).isEqualTo(GamePhase.ROUND_ONE_PLAY);
		assertThat(updated.activePlayerId()).isEqualTo("player-2");
		assertThat(updated.currentNode().type()).isEqualTo(NodeType.ACTOR);
	}

	@Test
	void winningRoundOneMovesToRoundTwoSetup() {
		GameState game = roundOnePlayOnActor();
		when(valueOperations.get(any())).thenReturn(game);

		WikidataNode fightClub = new WikidataNode("Q190050", "Fight Club", NodeType.FILM);
		when(wikidataService.resolveNode("Q190050")).thenReturn(fightClub);
		when(wikidataService.areAdjacent("Q35332", "Q190050")).thenReturn(true);

		GameState updated = gameService.applyMove(
				"game-123",
				new MoveRequest("player-2", "Q190050"));

		assertThat(updated.phase()).isEqualTo(GamePhase.ROUND_TWO_SETUP);
		assertThat(updated.round()).isEqualTo(2);
		assertThat(updated.activePlayerId()).isEqualTo("player-2");
	}

	@Test
	void configureRoundTwoStartsHostNavigation() {
		GameState game = roundTwoSetup();
		when(valueOperations.get(any())).thenReturn(game);
		when(wikidataService.resolveFilm("Q190908")).thenReturn(new WikidataNode("Q190908", "Seven", NodeType.FILM));
		when(wikidataService.resolveFilm("Q190050")).thenReturn(new WikidataNode("Q190050", "Fight Club", NodeType.FILM));

		GameState updated = gameService.configureRoundTwo(
				"game-123",
				new RoundSetupRequest("player-2", "Q190908", "Q190050"));

		assertThat(updated.phase()).isEqualTo(GamePhase.ROUND_TWO_PLAY);
		assertThat(updated.activePlayerId()).isEqualTo("player-1");
		assertThat(updated.clickCount()).isZero();
	}

	@Test
	void giveUpRoundOneHandsSetupToNavigator() {
		GameState game = roundOnePlayOnMovie();
		when(valueOperations.get(any())).thenReturn(game);

		GameState updated = gameService.giveUp("game-123", new GiveUpRequest("player-2"));

		assertThat(updated.phase()).isEqualTo(GamePhase.ROUND_TWO_SETUP);
		assertThat(updated.activePlayerId()).isEqualTo("player-2");
		verify(broadcastService).publishState(updated);
	}

	@Test
	void giveUpRoundTwoFinishesGame() {
		GameState game = roundTwoPlayOnActor();
		when(valueOperations.get(any())).thenReturn(game);

		GameState updated = gameService.giveUp("game-123", new GiveUpRequest("player-1"));

		assertThat(updated.phase()).isEqualTo(GamePhase.FINISHED);
	}

	@Test
	void endMatchFinishesGameForEitherPlayer() {
		GameState game = roundOnePlayOnMovie();
		when(valueOperations.get(any())).thenReturn(game);

		GameState updated = gameService.endMatch("game-123", new GiveUpRequest("player-1"));

		assertThat(updated.phase()).isEqualTo(GamePhase.FINISHED);
		verify(broadcastService).publishState(updated);
	}

	@Test
	void spectatorCannotMove() {
		GameState game = roundOnePlayOnMovie();
		when(valueOperations.get(any())).thenReturn(game);

		assertThatThrownBy(() -> gameService.applyMove(
				"game-123",
				new MoveRequest("player-1", "Q35332")))
			.isInstanceOf(NotActivePlayerException.class);
	}

	private static GameState roundOnePlayOnMovie() {
		return baseState(GamePhase.ROUND_ONE_PLAY, 1, "Q190908", "Seven", "player-2", 0);
	}

	private static GameState roundOnePlayOnActor() {
		GameState base = baseState(GamePhase.ROUND_ONE_PLAY, 1, "Q35332", "Brad Pitt", "player-2", 1);
		WikidataNode start = new WikidataNode("Q190908", "Seven", NodeType.FILM);
		WikidataNode brad = new WikidataNode("Q35332", "Brad Pitt", NodeType.ACTOR);
		WikidataNode target = new WikidataNode("Q190050", "Fight Club", NodeType.FILM);
		return new GameState(
				base.gameId(),
				base.phase(),
				base.round(),
				start,
				brad,
				target,
				base.activePlayerId(),
				base.clickCount(),
				base.playerOne(),
				base.playerTwo(),
				base.createdAt());
	}

	private static GameState roundTwoSetup() {
		return baseState(GamePhase.ROUND_TWO_SETUP, 2, "Q190050", "Fight Club", "player-2", 2);
	}

	private static GameState roundTwoPlayOnActor() {
		GameState base = baseState(GamePhase.ROUND_TWO_PLAY, 2, "Q35332", "Brad Pitt", "player-1", 1);
		WikidataNode start = new WikidataNode("Q219374", "Se7en", NodeType.FILM);
		WikidataNode brad = new WikidataNode("Q35332", "Brad Pitt", NodeType.ACTOR);
		WikidataNode target = new WikidataNode("Q190050", "Fight Club", NodeType.FILM);
		return new GameState(
				base.gameId(),
				base.phase(),
				base.round(),
				start,
				brad,
				target,
				base.activePlayerId(),
				base.clickCount(),
				base.playerOne(),
				base.playerTwo(),
				base.createdAt());
	}

	private static GameState baseState(
			GamePhase phase,
			int round,
			String currentId,
			String currentLabel,
			String navigatorId,
			int clicks) {
		GamePlayer alice = new GamePlayer("player-1", "Alice", PlayerSlot.ONE);
		GamePlayer bob = new GamePlayer("player-2", "Bob", PlayerSlot.TWO);
		WikidataNode start = new WikidataNode("Q190908", "Seven", NodeType.FILM);
		NodeType currentType = currentId.equals("Q35332") ? NodeType.ACTOR : NodeType.FILM;
		WikidataNode current = new WikidataNode(currentId, currentLabel, currentType);
		WikidataNode target = new WikidataNode("Q190050", "Fight Club", NodeType.FILM);
		return new GameState(
				"game-123",
				phase,
				round,
				start,
				current,
				target,
				navigatorId,
				clicks,
				alice,
				bob,
				Instant.parse("2026-05-20T22:00:00Z"));
	}

}
