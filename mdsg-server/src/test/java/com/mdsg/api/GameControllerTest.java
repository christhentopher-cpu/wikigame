package com.mdsg.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mdsg.domain.GamePhase;
import com.mdsg.domain.GamePlayer;
import com.mdsg.domain.GameState;
import com.mdsg.domain.NodeType;
import com.mdsg.domain.PlayerSlot;
import com.mdsg.domain.WikidataNode;
import com.mdsg.game.GameNotFoundException;
import com.mdsg.game.GameService;

@WebMvcTest(controllers = GameController.class)
@Import(ApiExceptionHandler.class)
class GameControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GameService gameService;

	@Test
	void joinGameStartsRoundOneWithGuestNavigating() throws Exception {
		GamePlayer guest = new GamePlayer("player-2", "Bob", PlayerSlot.TWO);
		GameState joined = new GameState(
				"game-123",
				GamePhase.ROUND_ONE_PLAY,
				1,
				sampleState().startMovie(),
				sampleState().startMovie(),
				sampleState().targetMovie(),
				"player-2",
				0,
				sampleState().playerOne(),
				guest,
				Instant.parse("2026-05-20T22:00:00Z"));
		when(gameService.joinGame(any(), any())).thenReturn(joined);

		mockMvc.perform(post("/api/games/game-123/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "playerName": "Bob" }
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.state.phase").value("ROUND_ONE_PLAY"))
			.andExpect(jsonPath("$.state.activePlayerId").value("player-2"));
	}

	@Test
	void getGameReturnsState() throws Exception {
		when(gameService.getGame("game-123")).thenReturn(sampleState());

		mockMvc.perform(get("/api/games/game-123"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.startMovie.label").value("Seven"));
	}

	@Test
	void getGameReturns404WhenMissing() throws Exception {
		when(gameService.getGame("missing")).thenThrow(new GameNotFoundException("missing"));

		mockMvc.perform(get("/api/games/missing"))
			.andExpect(status().isNotFound());
	}

	private static GameState sampleState() {
		GamePlayer host = new GamePlayer("player-1", "Alice", PlayerSlot.ONE);
		WikidataNode start = new WikidataNode("Q190908", "Seven", NodeType.FILM);
		WikidataNode target = new WikidataNode("Q190050", "Fight Club", NodeType.FILM);
		return new GameState(
				"game-123",
				GamePhase.WAITING_FOR_OPPONENT,
				1,
				start,
				start,
				target,
				"player-1",
				0,
				host,
				null,
				Instant.parse("2026-05-20T22:00:00Z"));
	}

}
