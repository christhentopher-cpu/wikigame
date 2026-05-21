package com.mdsg.wikidata;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdsg.domain.WikidataNode;

@Component
public class WikidataNeighborCache {

	static final String KEY_PREFIX = "wikidata:neighbors:";

	private final StringRedisTemplate redis;
	private final ObjectMapper objectMapper;
	private final Duration ttl;

	public WikidataNeighborCache(StringRedisTemplate redis, WikidataProperties properties) {
		this.redis = redis;
		this.objectMapper = new ObjectMapper();
		this.ttl = properties.getNeighborCacheTtl();
	}

	public Optional<List<WikidataNode>> get(String nodeId) {
		String json = redis.opsForValue().get(KEY_PREFIX + nodeId);
		if (json == null || json.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(objectMapper.readValue(json, new TypeReference<List<WikidataNode>>() {
			}));
		}
		catch (JsonProcessingException ex) {
			redis.delete(KEY_PREFIX + nodeId);
			return Optional.empty();
		}
	}

	public void put(String nodeId, List<WikidataNode> neighbors) {
		try {
			String json = objectMapper.writeValueAsString(neighbors);
			redis.opsForValue().set(KEY_PREFIX + nodeId, json, ttl);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to cache neighbors for " + nodeId, ex);
		}
	}

}
