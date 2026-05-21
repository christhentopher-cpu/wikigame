package com.wikigame.api;

import java.util.List;

import com.wikigame.domain.WikidataNode;

public record NeighborsResponse(String gameId, WikidataNode currentNode, List<WikidataNode> neighbors) {
}
