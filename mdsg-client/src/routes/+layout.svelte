<script lang="ts">
	import favicon from '$lib/assets/favicon.svg';

	let { children } = $props();
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
	<title>MDSG — Movie Database Search Game</title>
</svelte:head>

<div class="app">
	<header class="top">
		<a href="/" class="brand">MDSG</a>
		<span class="tagline">Movie Database Search Game</span>
	</header>
	<main>{@render children()}</main>
</div>

<style>
	:global(body) {
		margin: 0;
		font-family:
			system-ui,
			-apple-system,
			'Segoe UI',
			Roboto,
			sans-serif;
		background: #0f1419;
		color: #e7ecf3;
		min-height: 100vh;
	}

	:global(a) {
		color: #7eb8ff;
	}

	:global(button) {
		font: inherit;
		cursor: pointer;
	}

	:global(input) {
		font: inherit;
	}

	.app {
		max-width: 720px;
		margin: 0 auto;
		padding: 1.25rem 1rem 3rem;
	}

	@media (max-width: 480px) {
		.app {
			padding: 1rem 0.75rem 2.5rem;
		}

		.top {
			flex-direction: column;
			align-items: flex-start;
			gap: 0.35rem;
		}

		.tagline {
			font-size: 0.8rem;
		}
	}

	.top {
		display: flex;
		align-items: baseline;
		gap: 0.75rem;
		margin-bottom: 1.5rem;
		padding-bottom: 0.75rem;
		border-bottom: 1px solid #243044;
	}

	.brand {
		font-size: 1.35rem;
		font-weight: 700;
		text-decoration: none;
		color: #fff;
	}

	.tagline {
		font-size: 0.85rem;
		color: #8b9cb3;
	}
</style>
