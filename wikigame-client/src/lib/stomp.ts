import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import { WS_URL } from './config';
import type { GameState } from './types';

export function connectGame(gameId: string, onState: (state: GameState) => void): Client {
	const topic = `/topic/game/${gameId}`;
	const client = new Client({
		webSocketFactory: () => new SockJS(WS_URL),
		reconnectDelay: 3000,
		onConnect: () => {
			client.subscribe(topic, (message) => {
				onState(JSON.parse(message.body) as GameState);
			});
		}
	});

	client.activate();
	return client;
}
