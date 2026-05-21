<script lang="ts">
	import { onDestroy, onMount } from 'svelte';
	import { page } from '$app/state';
	import type { Client } from '@stomp/stompjs';

	import MovieSearch from '$lib/components/MovieSearch.svelte';
	import * as api from '$lib/api';
	import { getPlayerSession, setPlayerSession, type PlayerSession } from '$lib/session';
	import { connectGame } from '$lib/stomp';
	import type { GameState, WikidataNode } from '$lib/types';

	const gameId = $derived.by(() => {
		const id = page.params.gameId;
		if (!id) {
			throw new Error('Missing game id');
		}
		return id;
	});

	let session = $state<PlayerSession | null>(null);
	let game = $state<GameState | null>(null);
	let neighbors = $state<WikidataNode[]>([]);
	let neighborsLoading = $state(false);
	let neighborsError = $state<string | null>(null);
	let neighborsRequestId = 0;
	let joinName = $state('Bob');
	let round2StartId = $state('');
	let round2StartLabel = $state('');
	let round2TargetId = $state('');
	let round2TargetLabel = $state('');
	let loading = $state(true);
	let moving = $state(false);
	let error = $state<string | null>(null);
	let stompClient: Client | null = null;

	const isNavigator = $derived(
		game && session ? game.activePlayerId === session.playerId : false
	);

	const isPlayPhase = $derived(
		game?.phase === 'ROUND_ONE_PLAY' || game?.phase === 'ROUND_TWO_PLAY'
	);

	const inviteUrl = $derived(
		typeof window !== 'undefined' ? `${window.location.origin}/play/${gameId}` : ''
	);

	const opponentLabel = $derived.by(() => {
		if (!game || !session) {
			return 'your opponent';
		}
		if (game.playerOne.id === session.playerId) {
			return game.playerTwo?.displayName ?? 'Guest';
		}
		return game.playerOne.displayName;
	});

	const roleBanner = $derived.by(() => {
		if (!game || !session) {
			return '';
		}
		if (game.phase === 'WAITING_FOR_OPPONENT') {
			return session.slot === 'ONE'
				? 'Waiting for guest — they will play your puzzle first.'
				: '';
		}
		if (game.phase === 'ROUND_TWO_SETUP') {
			return isNavigator
				? `Choose start and destination movies for ${opponentLabel} (round 2).`
				: `${opponentLabel} is choosing your round 2 movies…`;
		}
		if (game.phase === 'FINISHED') {
			return 'Both rounds complete.';
		}
		if (isNavigator) {
			const round =
				game.phase === 'ROUND_ONE_PLAY'
					? 'Round 1 — reach the destination'
					: 'Round 2 — reach the destination';
			return round;
		}
		return 'Watching ' + opponentLabel + ' navigate…';
	});

	const linkHeading = $derived.by(() => {
		if (!game || game.phase === 'FINISHED') {
			return 'Game over';
		}
		if (!isPlayPhase) {
			return '';
		}
		if (!isNavigator) {
			return 'Spectating';
		}
		return game.currentNode.type === 'FILM' ? 'Cast — pick an actor' : 'Filmography — pick a movie';
	});

	const linkHint = $derived.by(() => {
		if (!game || !isNavigator) {
			return '';
		}
		return game.currentNode.type === 'FILM'
			? 'Actors who appear in this film.'
			: 'Films this actor appeared in.';
	});

	$effect(() => {
		const nodeId = game?.currentNode?.id;
		const playPhase = isPlayPhase;
		if (!nodeId || !playPhase) {
			neighbors = [];
			neighborsLoading = false;
			neighborsError = null;
			return;
		}
		void loadNeighbors(nodeId);
	});

	onMount(async () => {
		session = getPlayerSession(gameId);
		await refreshGame();
		stompClient = connectGame(gameId, (state) => {
			game = state;
		});
	});

	onDestroy(() => {
		stompClient?.deactivate();
	});

	async function refreshGame() {
		loading = true;
		error = null;
		try {
			game = await api.getGame(gameId);
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to load game';
		} finally {
			loading = false;
		}
	}

	async function loadNeighbors(nodeId: string, retry = true) {
		const requestId = ++neighborsRequestId;
		neighborsLoading = true;
		neighborsError = null;
		try {
			const response = await api.getNeighbors(gameId, nodeId);
			if (requestId !== neighborsRequestId) {
				return;
			}
			if (response.currentNode.id !== nodeId) {
				if (retry) {
					game = await api.getGame(gameId);
					const latestId = game?.currentNode?.id;
					if (latestId) {
						await loadNeighbors(latestId, false);
					}
				}
				return;
			}
			neighbors = response.neighbors;
		} catch (e) {
			if (requestId !== neighborsRequestId) {
				return;
			}
			const message = e instanceof Error ? e.message : 'Could not load links from Wikidata';
			if (retry && message.includes('position changed')) {
				game = await api.getGame(gameId);
				const latestId = game?.currentNode?.id;
				if (latestId) {
					await loadNeighbors(latestId, false);
					return;
				}
			}
			neighbors = [];
			neighborsError = message;
		} finally {
			if (requestId === neighborsRequestId) {
				neighborsLoading = false;
			}
		}
	}

	async function handleJoin(event: SubmitEvent) {
		event.preventDefault();
		loading = true;
		error = null;
		try {
			const response = await api.joinGame(gameId, { playerName: joinName.trim() });
			session = {
				playerId: response.playerId,
				displayName: joinName.trim(),
				slot: 'TWO'
			};
			setPlayerSession(gameId, session);
			game = response.state;
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to join';
		} finally {
			loading = false;
		}
	}

	async function handleRoundSetup(event: SubmitEvent) {
		event.preventDefault();
		if (!session || !round2StartId || !round2TargetId) {
			error = 'Search and select both movies from the list.';
			return;
		}
		loading = true;
		error = null;
		try {
			game = await api.configureRoundTwo(gameId, {
				playerId: session.playerId,
				startMovieId: round2StartId,
				targetMovieId: round2TargetId
			});
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to set up round 2';
		} finally {
			loading = false;
		}
	}

	async function handleMove(node: WikidataNode) {
		if (!session || !isNavigator || moving || !isPlayPhase) {
			return;
		}
		moving = true;
		error = null;
		try {
			game = await api.move(gameId, { playerId: session.playerId, nodeId: node.id });
		} catch (e) {
			error = e instanceof Error ? e.message : 'Move failed';
		} finally {
			moving = false;
		}
	}

	async function handleEndMatch() {
		if (!session || game?.phase === 'FINISHED') {
			return;
		}
		if (!confirm('End this match for both players?')) {
			return;
		}
		moving = true;
		error = null;
		try {
			game = await api.endMatch(gameId, { playerId: session.playerId });
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to end match';
		} finally {
			moving = false;
		}
	}

	async function handleGiveUp() {
		if (!session || !isNavigator || !isPlayPhase) {
			return;
		}
		const message =
			game?.phase === 'ROUND_ONE_PLAY'
				? `Give up round 1? You'll choose ${opponentLabel}'s round 2 movies.`
				: 'Give up round 2? The game will end.';
		if (!confirm(message)) {
			return;
		}
		moving = true;
		error = null;
		try {
			game = await api.giveUp(gameId, { playerId: session.playerId });
		} catch (e) {
			error = e instanceof Error ? e.message : 'Give up failed';
		} finally {
			moving = false;
		}
	}

	async function copyInvite() {
		if (!inviteUrl) {
			return;
		}
		await navigator.clipboard.writeText(inviteUrl);
	}
</script>

{#if loading && !game}
	<p class="status">Loading game…</p>
{:else if error && !game}
	<p class="error">{error}</p>
{:else if game}
	{#if !session}
		<section class="card">
			<h1>Join game</h1>
			<p class="hint">
				{game.playerOne.displayName} set the movies. You’ll navigate first while they watch; then
				you’ll pick movies for their turn.
			</p>
			<form onsubmit={handleJoin} class="form">
				<label>
					Your name
					<input bind:value={joinName} required maxlength="50" />
				</label>
				{#if error}
					<p class="error">{error}</p>
				{/if}
				<button type="submit" class="primary" disabled={loading}>Join & play</button>
			</form>
		</section>
	{:else}
		<section class="meta card">
			<div>
				<span class="label">You</span>
				<strong>{session.displayName}</strong>
				<span class="pill">{session.slot === 'ONE' ? 'Host' : 'Guest'}</span>
			</div>
			<div>
				<span class="label">Round</span>
				<strong>{game.round}</strong>
			</div>
			<div>
				<span class="label">Clicks this round</span>
				<strong>{game.clickCount}</strong>
			</div>
			{#if game.phase === 'WAITING_FOR_OPPONENT'}
				<button type="button" class="ghost" onclick={copyInvite}>Copy invite link</button>
			{/if}
			{#if game.phase !== 'FINISHED'}
				<button type="button" class="ghost end-match" disabled={moving} onclick={handleEndMatch}>
					End match
				</button>
			{/if}
		</section>

		{#if game.phase === 'FINISHED'}
			<section class="card finished">
				<h2>Match over</h2>
				<p class="hint">Both rounds are done or someone ended the game. Start fresh with a new link.</p>
				<a href="/" class="primary-link">New game</a>
			</section>
		{/if}

		{#if roleBanner}
			<p class="banner" class:watching={!isNavigator && isPlayPhase}>{roleBanner}</p>
		{/if}

		{#if game.phase === 'ROUND_TWO_SETUP' && isNavigator}
			<section class="card">
				<h2>Set movies for {opponentLabel}</h2>
				<p class="hint">Choose where they start and where they must end.</p>
				<form onsubmit={handleRoundSetup} class="form">
					<MovieSearch
						label="Start movie"
						bind:movieId={round2StartId}
						bind:movieLabel={round2StartLabel}
					/>
					<MovieSearch
						label="Destination movie"
						bind:movieId={round2TargetId}
						bind:movieLabel={round2TargetLabel}
					/>
					<button
						type="submit"
						class="primary"
						disabled={loading || !round2StartId || !round2TargetId}
					>
						Start round 2
					</button>
				</form>
			</section>
		{:else if isPlayPhase || game.phase === 'FINISHED'}
			<section class="card route">
				<div class="endpoint">
					<span class="label">Start</span>
					<p class="node">
						{game.startMovie.label}
						<code>{game.startMovie.id}</code>
					</p>
				</div>
				<div class="arrow">→</div>
				<div class="endpoint dest">
					<span class="label">Destination</span>
					<p class="node">
						{game.targetMovie.label}
						<code>{game.targetMovie.id}</code>
					</p>
				</div>
			</section>

			<section class="card">
				<h2>You are here</h2>
				<p class="node current">
					<span class="type">{game.currentNode.type === 'FILM' ? 'MOVIE' : 'ACTOR'}</span>
					{game.currentNode.label}
					<code>{game.currentNode.id}</code>
				</p>
			</section>

			{#if game.phase !== 'FINISHED'}
				<section class="card">
					<div class="section-head">
						{#if linkHeading}
							<h2>{linkHeading}</h2>
						{/if}
						{#if isNavigator}
							<button
								type="button"
								class="give-up"
								disabled={moving}
								onclick={handleGiveUp}
							>
								Give up
							</button>
						{/if}
					</div>
					{#if linkHint}
						<p class="hint">{linkHint}</p>
					{/if}

					{#if neighborsLoading}
						<p class="hint">Loading from Wikidata…</p>
					{:else if neighborsError}
						<p class="error">{neighborsError}</p>
						<button
							type="button"
							class="ghost"
							onclick={() => game?.currentNode?.id && loadNeighbors(game.currentNode.id)}
						>
							Retry
						</button>
					{:else if neighbors.length === 0}
						<p class="hint">No links found for this page on Wikidata.</p>
					{:else}
						<ul class="links">
							{#each neighbors as node (node.id)}
								<li>
									<button
										type="button"
										class="link"
										disabled={!isNavigator || moving}
										onclick={() => handleMove(node)}
									>
										<span class="type">{node.type}</span>
										{node.label}
										<code>{node.id}</code>
									</button>
								</li>
							{/each}
						</ul>
					{/if}
				</section>
			{/if}
		{:else if game.phase === 'WAITING_FOR_OPPONENT' && session.slot === 'ONE'}
			<section class="card">
				<p class="hint">Share the invite link. Your guest plays round 1 on the movies above.</p>
			</section>
		{/if}

		{#if error}
			<p class="error">{error}</p>
		{/if}
	{/if}
{/if}

<style>
	.card {
		background: #1a2332;
		border: 1px solid #2a3a52;
		border-radius: 12px;
		padding: 1.1rem 1.25rem;
		margin-bottom: 1rem;
	}

	h1,
	h2 {
		margin: 0 0 0.75rem;
		font-size: 1.15rem;
	}

	.section-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 0.75rem;
		margin-bottom: 0.5rem;
	}

	.section-head h2 {
		margin: 0;
	}

	.give-up {
		padding: 0.4rem 0.75rem;
		border-radius: 8px;
		border: 1px solid #8b3a3a;
		background: transparent;
		color: #ff9b9b;
		font-size: 0.85rem;
		white-space: nowrap;
	}

	.give-up:hover:not(:disabled) {
		background: #3a2020;
	}

	.give-up:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.hint {
		margin: 0 0 0.75rem;
		color: #9aadc4;
		font-size: 0.9rem;
	}

	.banner {
		margin: 0 0 1rem;
		padding: 0.75rem 1rem;
		border-radius: 8px;
		background: #243044;
		color: #c5d8f0;
		font-size: 0.95rem;
		line-height: 1.4;
	}

	.banner.watching {
		background: #1e2a3d;
		color: #9aadc4;
		border: 1px dashed #3a4f6b;
	}

	.status {
		color: #9aadc4;
	}

	.meta {
		display: flex;
		flex-wrap: wrap;
		gap: 1rem 1.5rem;
		align-items: center;
	}

	.end-match {
		color: #e8a0a0;
		border-color: #5a3a3a;
	}

	.finished {
		text-align: center;
	}

	.primary-link {
		display: inline-block;
		margin-top: 0.5rem;
		padding: 0.65rem 1.25rem;
		border-radius: 8px;
		background: #3d7eff;
		color: #fff;
		font-weight: 600;
		text-decoration: none;
	}

	.meta .label {
		display: block;
		font-size: 0.75rem;
		color: #8b9cb3;
		text-transform: uppercase;
		letter-spacing: 0.04em;
	}

	.pill {
		margin-left: 0.5rem;
		padding: 0.1rem 0.45rem;
		border-radius: 999px;
		background: #2a3a52;
		font-size: 0.75rem;
	}

	.node {
		margin: 0;
	}

	.route {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		flex-wrap: wrap;
	}

	.endpoint {
		flex: 1;
		min-width: 140px;
	}

	.endpoint .label {
		display: block;
		font-size: 0.75rem;
		color: #8b9cb3;
		text-transform: uppercase;
		margin-bottom: 0.35rem;
	}

	.endpoint.dest .node {
		color: #9ed8ff;
	}

	.arrow {
		font-size: 1.25rem;
		color: #5a8fd4;
		font-weight: 700;
	}

	.type {
		display: inline-block;
		margin-right: 0.5rem;
		padding: 0.1rem 0.4rem;
		border-radius: 4px;
		background: #2d4058;
		font-size: 0.7rem;
		font-weight: 600;
	}

	code {
		display: block;
		margin-top: 0.25rem;
		font-size: 0.8rem;
		color: #8b9cb3;
	}

	.links {
		list-style: none;
		margin: 0;
		padding: 0;
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
		max-height: 360px;
		overflow-y: auto;
	}

	.link {
		width: 100%;
		text-align: left;
		padding: 0.65rem 0.75rem;
		border-radius: 8px;
		border: 1px solid #3a4f6b;
		background: #0f1419;
		color: #e7ecf3;
	}

	.link:hover:not(:disabled) {
		border-color: #5a8fd4;
		background: #152030;
	}

	.link:disabled {
		opacity: 0.55;
		cursor: not-allowed;
	}

	.form {
		display: flex;
		flex-direction: column;
		gap: 0.75rem;
	}

	label {
		display: flex;
		flex-direction: column;
		gap: 0.35rem;
		font-size: 0.85rem;
		color: #b8c7db;
	}

	input {
		padding: 0.55rem 0.65rem;
		border-radius: 8px;
		border: 1px solid #3a4f6b;
		background: #0f1419;
		color: #fff;
	}

	.primary {
		padding: 0.65rem 1rem;
		border: none;
		border-radius: 8px;
		background: #3d7eff;
		color: #fff;
		font-weight: 600;
	}

	.ghost {
		padding: 0.4rem 0.75rem;
		border-radius: 8px;
		border: 1px solid #3a4f6b;
		background: transparent;
		color: #b8c7db;
	}

	.error {
		margin: 0.75rem 0 0;
		color: #ff8f8f;
		font-size: 0.9rem;
	}
</style>
