package com.mdsg.wikidata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.mdsg.domain.NodeType;
import com.mdsg.domain.WikidataNode;

final class WikidataLabelEnricher {

	private WikidataLabelEnricher() {
	}

	static List<WikidataNode> enrich(
			List<WikidataNode> nodes,
			Function<Collection<String>, Map<String, String>> labelFetcher) {
		if (nodes.isEmpty()) {
			return List.of();
		}

		List<String> needsLookup = nodes.stream()
				.filter(node -> !WikidataLabels.isDisplayable(node.id(), node.label()))
				.map(WikidataNode::id)
				.distinct()
				.toList();

		Map<String, String> fetched = needsLookup.isEmpty() ? Map.of() : labelFetcher.apply(needsLookup);

		List<WikidataNode> enriched = new ArrayList<>(nodes.size());
		for (WikidataNode node : nodes) {
			String label = node.label();
			if (!WikidataLabels.isDisplayable(node.id(), label)) {
				label = fetched.get(node.id());
			}
			if (WikidataLabels.isDisplayable(node.id(), label)) {
				enriched.add(new WikidataNode(node.id(), label.trim(), node.type()));
			}
		}
		return enriched;
	}

	static WikidataNode enrichOne(WikidataNode node, Function<Collection<String>, Map<String, String>> labelFetcher) {
		List<WikidataNode> result = enrich(List.of(node), labelFetcher);
		return result.isEmpty() ? null : result.get(0);
	}

}
