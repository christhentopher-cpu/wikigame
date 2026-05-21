package com.mdsg.wikidata;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.mdsg.api.FilmSearchResult;
import com.mdsg.domain.NodeType;
import com.mdsg.domain.WikidataNode;

@Component
public class WikidataClient {

	private final RestClient restClient;
	private final WikidataProperties properties;

	public WikidataClient(RestClient wikidataRestClient, WikidataProperties properties) {
		this.restClient = wikidataRestClient;
		this.properties = properties;
	}

	public boolean ask(String sparql) {
		JsonNode root = executeSparql(sparql);
		return root.path("boolean").asBoolean(false);
	}

	public List<WikidataNode> selectNodes(String sparql, String entityVar, String labelVar, NodeType type) {
		JsonNode bindings = executeSparql(sparql).path("results").path("bindings");
		List<WikidataNode> nodes = new ArrayList<>();
		if (!bindings.isArray()) {
			return nodes;
		}
		for (JsonNode binding : bindings) {
			String id = extractQid(binding.path(entityVar).path("value").asText(null));
			String label = binding.path(labelVar).path("value").asText(null);
			if (id != null && label != null) {
				nodes.add(new WikidataNode(id, label, type));
			}
		}
		return nodes;
	}

	public WikidataNode selectSingleNode(String sparql, String entityVar, String labelVar, NodeType type) {
		List<WikidataNode> nodes = selectNodes(sparql, entityVar, labelVar, type);
		if (nodes.isEmpty()) {
			return null;
		}
		return nodes.get(0);
	}

	public String selectLabel(String sparql, String labelVar) {
		JsonNode bindings = executeSparql(sparql).path("results").path("bindings");
		if (!bindings.isArray() || bindings.isEmpty()) {
			return null;
		}
		return bindings.get(0).path(labelVar).path("value").asText(null);
	}

	public List<FilmSearchResult> searchEntities(String query, int limit) {
		URI uri = UriComponentsBuilder.fromUriString(properties.getApiUrl())
			.queryParam("action", "wbsearchentities")
			.queryParam("search", query)
			.queryParam("language", "en")
			.queryParam("format", "json")
			.queryParam("limit", limit)
			.build()
			.encode()
			.toUri();

		JsonNode root = executeGet("Wikidata entity search", uri, MediaType.APPLICATION_JSON);
		List<FilmSearchResult> results = new ArrayList<>();
		JsonNode search = root.path("search");
		if (!search.isArray()) {
			return results;
		}
		for (JsonNode hit : search) {
			String id = hit.path("id").asText(null);
			String label = hit.path("label").asText(null);
			String description = hit.path("description").asText("");
			if (id != null && label != null) {
				results.add(new FilmSearchResult(id, label, description));
			}
		}
		return results;
	}

	public List<FilmSearchResult> searchFilmsByLabel(String query, int limit) {
		String needle = query.trim().toLowerCase().replace("\"", "\\\"");
		String sparql = """
				SELECT ?item ?itemLabel WHERE {
				  ?item wdt:P31/wdt:P279* ?filmType .
				  VALUES ?filmType { wd:Q11424 wd:Q24869 wd:Q202860 }
				  ?item rdfs:label ?itemLabel .
				  FILTER(LANG(?itemLabel) = "en")
				  FILTER(CONTAINS(LCASE(STR(?itemLabel)), "%s"))
				}
				LIMIT %d
				""".formatted(needle, limit);

		JsonNode bindings = executeSparql(sparql).path("results").path("bindings");
		List<FilmSearchResult> results = new ArrayList<>();
		if (!bindings.isArray()) {
			return results;
		}
		for (JsonNode binding : bindings) {
			String id = extractQid(binding.path("item").path("value").asText(null));
			String label = binding.path("itemLabel").path("value").asText(null);
			if (id != null && label != null) {
				results.add(new FilmSearchResult(id, label, ""));
			}
		}
		return results;
	}

	private JsonNode executeSparql(String sparql) {
		URI uri = UriComponentsBuilder.fromUriString(properties.getSparqlUrl())
			.queryParam("query", sparql)
			.build()
			.encode()
			.toUri();

		return executeGet("Wikidata SPARQL", uri, MediaType.parseMediaType("application/sparql-results+json"));
	}

	private JsonNode executeGet(String operation, URI uri, MediaType accept) {
		return withRetry(operation, () -> restClient.get()
			.uri(uri)
			.accept(accept)
			.retrieve()
			.body(JsonNode.class));
	}

	private <T> T withRetry(String operation, Supplier<T> request) {
		int maxAttempts = Math.max(1, properties.getRetryMaxAttempts());
		long backoffMs = Math.max(0, properties.getRetryBackoffMs());
		RuntimeException lastFailure = null;

		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				return request.get();
			}
			catch (RestClientResponseException ex) {
				lastFailure = ex;
				if (!isRetryable(ex) || attempt == maxAttempts) {
					throw toUnavailable(operation, ex);
				}
				sleep(backoffMs * attempt);
			}
			catch (ResourceAccessException ex) {
				lastFailure = ex;
				if (attempt == maxAttempts) {
					throw toUnavailable(operation, ex);
				}
				sleep(backoffMs * attempt);
			}
			catch (RuntimeException ex) {
				throw toUnavailable(operation, ex);
			}
		}

		throw toUnavailable(operation, lastFailure);
	}

	private static boolean isRetryable(RestClientResponseException ex) {
		int status = ex.getStatusCode().value();
		return status == 429 || status == 502 || status == 503 || status == 504;
	}

	private static WikidataUnavailableException toUnavailable(String operation, Exception ex) {
		String detail = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
		return new WikidataUnavailableException(
				operation + " is temporarily unavailable. Wikidata may be overloaded — please try again in a few seconds. ("
						+ detail + ")");
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new WikidataUnavailableException("Wikidata request interrupted");
		}
	}

	static String extractQid(String entityUri) {
		if (entityUri == null) {
			return null;
		}
		int index = entityUri.lastIndexOf('/');
		if (index < 0 || index == entityUri.length() - 1) {
			return null;
		}
		String qid = entityUri.substring(index + 1);
		return qid.startsWith("Q") ? qid : null;
	}

	static String wd(String qid) {
		return "wd:" + qid;
	}

}
