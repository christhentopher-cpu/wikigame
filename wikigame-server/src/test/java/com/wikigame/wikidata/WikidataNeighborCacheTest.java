package com.wikigame.wikidata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.wikigame.domain.NodeType;
import com.wikigame.domain.WikidataNode;

@ExtendWith(MockitoExtension.class)
class WikidataNeighborCacheTest {

	@Mock
	private StringRedisTemplate redis;

	@Mock
	private ValueOperations<String, String> valueOperations;

	private WikidataNeighborCache cache;

	@BeforeEach
	void setUp() {
		when(redis.opsForValue()).thenReturn(valueOperations);
		WikidataProperties properties = new WikidataProperties();
		properties.setNeighborCacheTtl(Duration.ofMinutes(30));
		cache = new WikidataNeighborCache(redis, properties);
	}

	@Test
	void getReturnsEmptyWhenMissing() {
		when(valueOperations.get(anyString())).thenReturn(null);

		assertThat(cache.get("Q42")).isEmpty();
	}

	@Test
	void getReturnsCachedNeighbors() {
		when(valueOperations.get("wikidata:neighbors:Q42"))
				.thenReturn(
						"[{\"id\":\"Q1\",\"label\":\"Film\",\"type\":\"FILM\"}]");

		Optional<List<WikidataNode>> result = cache.get("Q42");

		assertThat(result).isPresent();
		assertThat(result.get()).containsExactly(new WikidataNode("Q1", "Film", NodeType.FILM));
	}

	@Test
	void putStoresJsonWithTtl() {
		List<WikidataNode> neighbors = List.of(new WikidataNode("Q1", "Film", NodeType.FILM));

		cache.put("Q99", neighbors);

		verify(valueOperations).set(eq("wikidata:neighbors:Q99"), anyString(), eq(Duration.ofMinutes(30)));
	}

}
