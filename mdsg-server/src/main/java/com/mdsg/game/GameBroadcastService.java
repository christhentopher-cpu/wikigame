package com.mdsg.game;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.mdsg.config.GameMessaging;
import com.mdsg.domain.GameState;

@Service
public class GameBroadcastService {

	private final SimpMessagingTemplate messagingTemplate;

	public GameBroadcastService(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void publishState(GameState state) {
		messagingTemplate.convertAndSend(GameMessaging.gameTopic(state.gameId()), state);
	}

}
