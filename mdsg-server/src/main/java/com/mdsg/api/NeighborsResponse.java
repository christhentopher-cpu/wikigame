package com.mdsg.api;

import java.util.List;

import com.mdsg.domain.WikidataNode;

public record NeighborsResponse(String gameId, WikidataNode currentNode, List<WikidataNode> neighbors) {
}
