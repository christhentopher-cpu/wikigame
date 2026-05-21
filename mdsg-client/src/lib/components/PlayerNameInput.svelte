<script lang="ts">
	interface Props {
		id?: string;
		value?: string;
		maxlength?: number;
		placeholder?: string;
		hint?: string;
	}

	let {
		id = 'player-name',
		value = $bindable(''),
		maxlength = 50,
		placeholder = 'How should we call you?',
		hint = 'Shown to your opponent in multiplayer'
	}: Props = $props();

	const length = $derived(value.length);
</script>

<div class="name-field">
	<label class="name-label" for={id}>Your name</label>
	<div class="name-input-wrap">
		<span class="avatar" aria-hidden="true">{value.trim() ? value.trim().charAt(0).toUpperCase() : '?'}</span>
		<input
			{id}
			class="name-input"
			type="text"
			bind:value
			{maxlength}
			{placeholder}
			required
			autocomplete="nickname"
			spellcheck="false"
		/>
	</div>
	<p class="name-meta">
		<span>{hint}</span>
		<span class="count" class:warn={length >= maxlength - 5}>{length}/{maxlength}</span>
	</p>
</div>

<style>
	.name-field {
		display: flex;
		flex-direction: column;
		gap: 0.45rem;
	}

	.name-label {
		font-size: 0.8rem;
		font-weight: 600;
		letter-spacing: 0.04em;
		text-transform: uppercase;
		color: #8b9cb3;
	}

	.name-input-wrap {
		display: flex;
		align-items: center;
		gap: 0.75rem;
		padding: 0.35rem 0.85rem 0.35rem 0.4rem;
		border-radius: 10px;
		border: 1px solid #3a4f6b;
		background: linear-gradient(145deg, #121a24 0%, #0c1016 100%);
		box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
		transition:
			border-color 0.15s ease,
			box-shadow 0.15s ease;
	}

	.name-input-wrap:focus-within {
		border-color: #5a8fd4;
		box-shadow:
			0 0 0 3px rgba(90, 143, 212, 0.2),
			inset 0 1px 0 rgba(255, 255, 255, 0.04);
	}

	.avatar {
		flex-shrink: 0;
		width: 2.25rem;
		height: 2.25rem;
		display: grid;
		place-items: center;
		border-radius: 8px;
		background: #2a4060;
		color: #c5d8f0;
		font-size: 1rem;
		font-weight: 700;
	}

	.name-input {
		flex: 1;
		min-width: 0;
		padding: 0.55rem 0.15rem;
		border: none;
		background: transparent;
		color: #f4f7fb;
		font-size: 1.05rem;
		font-weight: 500;
		outline: none;
	}

	.name-input::placeholder {
		color: #5c6f86;
		font-weight: 400;
	}

	.name-meta {
		display: flex;
		justify-content: space-between;
		align-items: center;
		gap: 0.75rem;
		margin: 0;
		font-size: 0.78rem;
		color: #6d8099;
	}

	.count {
		font-variant-numeric: tabular-nums;
		color: #8b9cb3;
	}

	.count.warn {
		color: #e8b86d;
	}
</style>
