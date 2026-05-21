export interface SoloPuzzle {
	id?: string;
	title?: string;
	startMovieId: string;
	targetMovieId: string;
	startMovieLabel?: string;
	targetMovieLabel?: string;
}

interface SoloPuzzleFile {
	puzzles: SoloPuzzle[];
}

export function puzzleLabel(puzzle: SoloPuzzle): string {
	if (puzzle.title?.trim()) {
		return puzzle.title.trim();
	}
	const start = puzzle.startMovieLabel?.trim() || puzzle.startMovieId;
	const end = puzzle.targetMovieLabel?.trim() || puzzle.targetMovieId;
	return `${start} → ${end}`;
}

export function isValidPuzzle(puzzle: SoloPuzzle): boolean {
	return (
		typeof puzzle.startMovieId === 'string' &&
		puzzle.startMovieId.startsWith('Q') &&
		typeof puzzle.targetMovieId === 'string' &&
		puzzle.targetMovieId.startsWith('Q') &&
		puzzle.startMovieId !== puzzle.targetMovieId
	);
}

export async function loadSoloPuzzles(): Promise<SoloPuzzle[]> {
	const response = await fetch('/solo-puzzles.json');
	if (!response.ok) {
		throw new Error('Could not load solo-puzzles.json');
	}
	const data = (await response.json()) as SoloPuzzleFile;
	if (!Array.isArray(data.puzzles)) {
		throw new Error('solo-puzzles.json must contain a "puzzles" array');
	}
	return data.puzzles.filter(isValidPuzzle);
}
