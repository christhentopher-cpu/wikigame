import type { WikidataNode } from './types';

/** Hide QID placeholders Wikidata returns when no English label exists. */
export function displayNodeLabel(node: { id: string; label: string }): string {
	const label = node.label?.trim() ?? '';
	if (!label) {
		return 'Unknown';
	}
	if (label.toUpperCase() === node.id.toUpperCase()) {
		return 'Unknown';
	}
	if (/^Q\d+$/i.test(label)) {
		return 'Unknown';
	}
	return label;
}

export function isDisplayableNode(node: WikidataNode): boolean {
	return displayNodeLabel(node) !== 'Unknown';
}
