package com.mdsg.wikidata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.mdsg.domain.NodeType;
import com.mdsg.domain.WikidataNode;

class WikidataLabelEnricherTest {

	@Test
	void replacesQidPlaceholderWithFetchedName() {
		List<WikidataNode> input = List.of(new WikidataNode("Q708059", "Q708059", NodeType.ACTOR));

		List<WikidataNode> result = WikidataLabelEnricher.enrich(
				input,
				ids -> Map.of("Q708059", "Chris Ellis"));

		assertThat(result).containsExactly(new WikidataNode("Q708059", "Chris Ellis", NodeType.ACTOR));
	}

	@Test
	void dropsNodesWithNoResolvableLabel() {
		List<WikidataNode> input = List.of(new WikidataNode("Q708059", "Q708059", NodeType.ACTOR));

		List<WikidataNode> result = WikidataLabelEnricher.enrich(input, ids -> Map.of());

		assertThat(result).isEmpty();
	}

	@Test
	void keepsNodesThatAlreadyHaveDisplayableLabels() {
		List<WikidataNode> input = List.of(new WikidataNode("Q103769", "Tim Allen", NodeType.ACTOR));

		List<WikidataNode> result = WikidataLabelEnricher.enrich(input, ids -> Map.of());

		assertThat(result).containsExactly(input.get(0));
	}

}
