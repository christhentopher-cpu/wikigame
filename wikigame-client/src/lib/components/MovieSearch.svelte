<script lang="ts">
	import { searchFilms, type FilmSearchResult } from '$lib/api';

	interface Props {
		label: string;
		movieId?: string;
		movieLabel?: string;
	}

	let {
		label,
		movieId = $bindable(''),
		movieLabel = $bindable('')
	}: Props = $props();

	let query = $state('');
	let results = $state<FilmSearchResult[]>([]);
	let open = $state(false);
	let searching = $state(false);
	let searchError = $state<string | null>(null);
	let debounceTimer: ReturnType<typeof setTimeout> | undefined;

	$effect(() => {
		if (movieLabel && !query) {
			query = movieLabel;
		}
	});

	function scheduleSearch(value: string) {
		clearTimeout(debounceTimer);
		movieId = '';
		movieLabel = '';
		if (value.trim().length < 2) {
			results = [];
			open = false;
			return;
		}
		debounceTimer = setTimeout(() => void runSearch(value), 300);
	}

	async function runSearch(value: string) {
		searching = true;
		searchError = null;
		try {
			results = await searchFilms(value);
			open = results.length > 0;
		} catch (e) {
			const msg = e instanceof Error ? e.message : 'Search failed';
			searchError = msg.includes('unavailable') || msg.includes('502')
				? 'Wikidata is busy — wait a moment and try again.'
				: msg;
			results = [];
			open = false;
		} finally {
			searching = false;
		}
	}

	function selectFilm(film: FilmSearchResult) {
		movieId = film.id;
		movieLabel = film.label;
		query = film.label;
		open = false;
	}

	function onInput(event: Event) {
		const value = (event.target as HTMLInputElement).value;
		query = value;
		scheduleSearch(value);
	}

	function onBlur() {
		setTimeout(() => {
			open = false;
		}, 150);
	}
</script>

<div class="search">
	<label>
		{label}
		<input
			type="text"
			value={query}
			oninput={onInput}
			onfocus={() => {
				if (results.length > 0) {
					open = true;
				}
			}}
			onblur={onBlur}
			placeholder="Search movies on Wikidata…"
			autocomplete="off"
		/>
	</label>

	{#if searching}
		<p class="status">Searching…</p>
	{:else if searchError}
		<p class="error">{searchError}</p>
	{/if}

	{#if movieId}
		<p class="selected">
			Selected: <strong>{movieLabel}</strong>
			<code>{movieId}</code>
		</p>
	{/if}

	{#if open}
		<ul class="results" role="listbox">
			{#each results as film (film.id)}
				<li>
					<button
						type="button"
						role="option"
						aria-selected={film.id === movieId}
						onclick={() => selectFilm(film)}
					>
						<span class="title">{film.label}</span>
						{#if film.description}
							<span class="desc">{film.description}</span>
						{/if}
					</button>
				</li>
			{/each}
		</ul>
	{/if}
</div>

<style>
	.search {
		position: relative;
		display: flex;
		flex-direction: column;
		gap: 0.35rem;
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

	.status {
		margin: 0;
		font-size: 0.8rem;
		color: #8b9cb3;
	}

	.selected {
		margin: 0;
		font-size: 0.85rem;
		color: #9aadc4;
	}

	.selected code {
		display: block;
		margin-top: 0.2rem;
		font-size: 0.75rem;
		color: #6d8099;
	}

	.results {
		position: absolute;
		top: 100%;
		left: 0;
		right: 0;
		z-index: 10;
		list-style: none;
		margin: 0.25rem 0 0;
		padding: 0.25rem;
		background: #0f1419;
		border: 1px solid #3a4f6b;
		border-radius: 8px;
		max-height: 240px;
		overflow-y: auto;
		box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
	}

	.results button {
		width: 100%;
		text-align: left;
		padding: 0.5rem 0.6rem;
		border: none;
		border-radius: 6px;
		background: transparent;
		color: #e7ecf3;
		cursor: pointer;
	}

	.results button:hover {
		background: #1a2838;
	}

	.title {
		display: block;
		font-weight: 600;
	}

	.desc {
		display: block;
		font-size: 0.8rem;
		color: #8b9cb3;
		margin-top: 0.15rem;
	}

	.error {
		margin: 0;
		color: #ff8f8f;
		font-size: 0.85rem;
	}
</style>
