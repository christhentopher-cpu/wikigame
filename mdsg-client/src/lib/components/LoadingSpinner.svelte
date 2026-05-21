<script lang="ts">
	interface Props {
		label?: string;
	}

	let { label = 'Loading…' }: Props = $props();
</script>

<p class="spinner-row" role="status" aria-live="polite">
	<span class="spinner" aria-hidden="true"></span>
	<span>{label}</span>
</p>

<style>
	.spinner-row {
		display: flex;
		align-items: center;
		gap: 0.6rem;
		margin: 0;
		color: #9aadc4;
		font-size: 0.9rem;
	}

	.spinner {
		width: 1rem;
		height: 1rem;
		border: 2px solid #3a4f6b;
		border-top-color: #5a8fd4;
		border-radius: 50%;
		animation: spin 0.7s linear infinite;
	}

	@keyframes spin {
		to {
			transform: rotate(360deg);
		}
	}
</style>
