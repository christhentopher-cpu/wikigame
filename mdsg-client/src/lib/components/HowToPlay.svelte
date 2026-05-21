<script lang="ts">
	let open = $state(false);
</script>

<section class="how-to">
	<button type="button" class="toggle" onclick={() => (open = !open)} aria-expanded={open}>
		{open ? '▼' : '▶'} How to play
	</button>

	{#if open}
		<ol class="steps">
			<li>
				<strong>Host</strong> picks a start movie and a destination movie (Wikidata search).
			</li>
			<li>
				<strong>Guest</strong> joins via invite link and navigates round 1 on the host’s movies.
			</li>
			<li>
				Each turn: from a <strong>movie</strong>, pick a cast member; from an <strong>actor</strong>, pick a
				film they appeared in.
			</li>
			<li>Reach the destination movie to finish the round. Guest then sets movies for the host’s round 2.</li>
			<li>Fewest clicks wins bragging rights — Wikidata can be slow, that’s normal.</li>
		</ol>
	{/if}
</section>

<style>
	.how-to {
		margin-bottom: 1rem;
	}

	.toggle {
		width: 100%;
		text-align: left;
		padding: 0.65rem 0.85rem;
		border-radius: 8px;
		border: 1px solid #2a3a52;
		background: #152030;
		color: #c5d8f0;
		font-weight: 600;
	}

	.toggle:hover {
		background: #1a2838;
	}

	.steps {
		margin: 0.75rem 0 0;
		padding-left: 1.25rem;
		color: #9aadc4;
		font-size: 0.9rem;
		line-height: 1.5;
	}

	.steps li {
		margin-bottom: 0.5rem;
	}

	.steps strong {
		color: #e7ecf3;
	}
</style>
