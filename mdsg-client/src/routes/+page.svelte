<script lang="ts">
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import HowToPlay from '$lib/components/HowToPlay.svelte';
	import MovieSearch from '$lib/components/MovieSearch.svelte';
	import PlayerNameInput from '$lib/components/PlayerNameInput.svelte';
	import { createGame, createSoloGame } from '$lib/api';
	import { loadSoloPuzzles, puzzleLabel, type SoloPuzzle } from '$lib/solo-puzzles';
	import { setPlayerSession } from '$lib/session';

	type HomeTab = 'multiplayer' | 'solo';

	let tab = $state<HomeTab>('multiplayer');

	let hostName = $state('Alice');
	let startMovieId = $state('');
	let startMovieLabel = $state('');
	let targetMovieId = $state('');
	let targetMovieLabel = $state('');
	let loading = $state(false);
	let error = $state<string | null>(null);

	let soloPuzzles = $state<SoloPuzzle[]>([]);
	let puzzlesLoading = $state(true);
	let puzzlesError = $state<string | null>(null);
	let selectedPuzzleId = $state<string | null>(null);

	onMount(async () => {
		try {
			soloPuzzles = await loadSoloPuzzles();
			if (soloPuzzles.length > 0) {
				selectedPuzzleId = soloPuzzles[0].id ?? soloPuzzles[0].startMovieId;
			}
		} catch (e) {
			puzzlesError = e instanceof Error ? e.message : 'Failed to load puzzles';
		} finally {
			puzzlesLoading = false;
		}
	});

	const selectedPuzzle = $derived(
		soloPuzzles.find((p) => (p.id ?? p.startMovieId) === selectedPuzzleId) ?? null
	);

	async function startGame(response: Awaited<ReturnType<typeof createGame>>) {
		setPlayerSession(response.gameId, {
			playerId: response.state.playerOne.id,
			displayName: response.state.playerOne.displayName,
			slot: 'ONE'
		});
		await goto(`/play/${response.gameId}`);
	}

	async function handleCreateMultiplayer(event: SubmitEvent) {
		event.preventDefault();
		if (!startMovieId || !targetMovieId) {
			error = 'Search and select both movies from the list.';
			return;
		}
		loading = true;
		error = null;
		try {
			const response = await createGame({
				startMovieId,
				targetMovieId,
				hostPlayerName: hostName.trim()
			});
			await startGame(response);
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to create game';
		} finally {
			loading = false;
		}
	}

	async function startSolo(puzzle: SoloPuzzle) {
		loading = true;
		error = null;
		try {
			const response = await createSoloGame({
				startMovieId: puzzle.startMovieId,
				targetMovieId: puzzle.targetMovieId,
				hostPlayerName: hostName.trim()
			});
			await startGame(response);
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to start solo game';
		} finally {
			loading = false;
		}
	}

	function playSelectedSolo() {
		if (!selectedPuzzle) {
			error = 'Choose a puzzle from the list.';
			return;
		}
		void startSolo(selectedPuzzle);
	}

</script>

<nav class="tabs" aria-label="Game mode">
	<button
		type="button"
		class:active={tab === 'multiplayer'}
		onclick={() => {
			tab = 'multiplayer';
			error = null;
		}}
	>
		Multiplayer
	</button>
	<button
		type="button"
		class:active={tab === 'solo'}
		onclick={() => {
			tab = 'solo';
			error = null;
		}}
	>
		Solo
	</button>
</nav>

{#if tab === 'multiplayer'}
	<HowToPlay />

	<section class="card">
		<h1>New multiplayer game</h1>
		<p class="hint">
			Search for real films on Wikidata — only verified movies can be selected. Your guest plays
			round 1 on your picks, then chooses films for your round.
		</p>

		<form onsubmit={handleCreateMultiplayer} class="form">
			<PlayerNameInput id="player-name-mp" bind:value={hostName} />

			<MovieSearch
				label="Start movie"
				bind:movieId={startMovieId}
				bind:movieLabel={startMovieLabel}
			/>

			<MovieSearch
				label="Destination movie"
				bind:movieId={targetMovieId}
				bind:movieLabel={targetMovieLabel}
			/>

			{#if error}
				<p class="error">{error}</p>
			{/if}

			<button type="submit" class="primary" disabled={loading || !startMovieId || !targetMovieId}>
				{loading ? 'Creating…' : 'Create game'}
			</button>
		</form>
	</section>
{:else}
	<section class="card">
		<h1>Solo</h1>
		<p class="hint">
			Play a puzzle from <code>static/solo-puzzles.json</code> — edit that file to add start/end
			movie pairs (Wikidata <code>Q…</code> IDs). See <code>solo-puzzles.README.md</code> in the same
			folder.
		</p>

		<PlayerNameInput
			id="player-name-solo"
			bind:value={hostName}
			placeholder="Solo player name"
			hint="Used on the score screen"
		/>

		{#if puzzlesLoading}
			<p class="hint">Loading puzzles…</p>
		{:else if puzzlesError}
			<p class="error">{puzzlesError}</p>
		{:else if soloPuzzles.length === 0}
			<p class="error">No valid puzzles in solo-puzzles.json.</p>
		{:else}
			<ul class="puzzle-list">
				{#each soloPuzzles as puzzle (puzzle.id ?? puzzle.startMovieId)}
					<li>
						<label class="puzzle-option">
							<input
								type="radio"
								name="solo-puzzle"
								value={puzzle.id ?? puzzle.startMovieId}
								bind:group={selectedPuzzleId}
							/>
							<span class="puzzle-text">
								<strong>{puzzleLabel(puzzle)}</strong>
								<span class="puzzle-ids">
									{puzzle.startMovieLabel ?? puzzle.startMovieId} →
									{puzzle.targetMovieLabel ?? puzzle.targetMovieId}
								</span>
							</span>
						</label>
					</li>
				{/each}
			</ul>

			<button type="button" class="primary play-btn" disabled={loading} onclick={playSelectedSolo}>
				{loading ? 'Starting…' : 'Play'}
			</button>
		{/if}

		{#if error}
			<p class="error">{error}</p>
		{/if}
	</section>
{/if}

<style>
	.tabs {
		display: flex;
		gap: 0.5rem;
		margin-bottom: 1rem;
	}

	.tabs button {
		flex: 1;
		padding: 0.6rem 1rem;
		border-radius: 8px;
		border: 1px solid #2a3a52;
		background: #152030;
		color: #9aadc4;
		font-weight: 600;
	}

	.tabs button.active {
		background: #243044;
		color: #fff;
		border-color: #5a8fd4;
	}

	.card {
		background: #1a2332;
		border: 1px solid #2a3a52;
		border-radius: 12px;
		padding: 1.25rem 1.5rem;
	}

	h1 {
		margin: 0 0 0.5rem;
		font-size: 1.4rem;
	}

	.hint {
		margin: 0 0 1.25rem;
		color: #9aadc4;
		font-size: 0.9rem;
		line-height: 1.45;
	}

	.hint code,
	.puzzle-ids {
		font-size: 0.8rem;
		color: #8b9cb3;
	}

	.form {
		display: flex;
		flex-direction: column;
		gap: 1rem;
	}

	.puzzle-list {
		list-style: none;
		margin: 1rem 0;
		padding: 0;
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
		max-height: 280px;
		overflow-y: auto;
	}

	.puzzle-option {
		display: flex;
		align-items: flex-start;
		gap: 0.6rem;
		padding: 0.65rem 0.75rem;
		border-radius: 8px;
		border: 1px solid #3a4f6b;
		background: #0f1419;
		cursor: pointer;
	}

	.puzzle-option:has(input:checked) {
		border-color: #5a8fd4;
		background: #152030;
	}

	.puzzle-text {
		display: flex;
		flex-direction: column;
		gap: 0.2rem;
	}

	.play-btn {
		width: 100%;
		margin-top: 0.25rem;
	}

	.primary {
		padding: 0.7rem 1rem;
		border: none;
		border-radius: 8px;
		background: #3d7eff;
		color: #fff;
		font-weight: 600;
	}

	.primary:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}

	.error {
		margin: 0.75rem 0 0;
		color: #ff8f8f;
		font-size: 0.9rem;
	}
</style>
