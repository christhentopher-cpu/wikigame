package com.wikigame.wikidata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wikigame.domain.NodeType;
import com.wikigame.domain.WikidataNode;

@ExtendWith(MockitoExtension.class)
class WikidataServiceCacheTest {

	@Mock
	private WikidataClient client;

	@Mock
	private WikidataNeighborCache neighborCache;

	private WikidataService wikidataService;

	@BeforeEach
	void setUp() {
		WikidataProperties properties = new WikidataProperties();
		wikidataService = new WikidataService(client, properties, neighborCache);
	}

	@Test
	void getNeighborsUsesCacheWithoutCallingSparql() {
		WikidataNode film = new WikidataNode("Q182153", "Cars", NodeType.FILM);
		List<WikidataNode> cached = List.of(new WikidataNode("Q1", "Actor", NodeType.ACTOR));
		when(client.selectSingleNode(any(), eq("item"), eq("itemLabel"), eq(NodeType.ACTOR))).thenReturn(null);
		when(client.selectSingleNode(any(), eq("item"), eq("itemLabel"), eq(NodeType.FILM))).thenReturn(film);
		when(neighborCache.get("Q182153")).thenReturn(Optional.of(cached));

		List<WikidataNode> result = wikidataService.getNeighbors(film);

		assertThat(result).isEqualTo(cached);
		verify(client, never()).selectNodes(any(), any(), any(), any());
		verify(neighborCache, never()).put(any(), any());
	}

}
