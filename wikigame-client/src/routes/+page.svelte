<script lang="ts">
	import { goto } from '$app/navigation';
	import MovieSearch from '$lib/components/MovieSearch.svelte';
	import { createGame } from '$lib/api';
	import { setPlayerSession } from '$lib/session';

	let hostName = $state('Alice');
	let startMovieId = $state('');
	let startMovieLabel = $state('');
	let targetMovieId = $state('');
	let targetMovieLabel = $state('');
	let loading = $state(false);
	let error = $state<string | null>(null);

	async function handleCreate(event: SubmitEvent) {
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
			setPlayerSession(response.gameId, {
				playerId: response.state.playerOne.id,
				displayName: response.state.playerOne.displayName,
				slot: 'ONE'
			});
			await goto(`/play/${response.gameId}`);
		} catch (e) {
			error = e instanceof Error ? e.message : 'Failed to create game';
		} finally {
			loading = false;
		}
	}
</script>

<section class="card">
	<h1>New game</h1>
	<p class="hint">
		Search for real films on Wikidata — only verified movies can be selected. Your guest plays
		round 1 on your picks, then chooses films for your round.
	</p>

	<form onsubmit={handleCreate} class="form">
		<label>
			Your name
			<input bind:value={hostName} required maxlength="50" />
		</label>

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

<style>
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

	.form {
		display: flex;
		flex-direction: column;
		gap: 1rem;
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
		margin-top: 0.25rem;
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
		margin: 0;
		color: #ff8f8f;
		font-size: 0.9rem;
	}
</style>
