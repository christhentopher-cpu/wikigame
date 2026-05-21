package com.mdsg.wikidata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.mdsg.api.FilmSearchResult;
import com.mdsg.domain.NodeType;
import com.mdsg.domain.WikidataNode;
import com.mdsg.game.InvalidMoveException;

@Service
public class WikidataService {

	private static final String HUMAN = "Q5";
	private static final String FILM = "Q11424";

	/** Wikidata classes treated as navigable films (subclass closure applied in SPARQL). */
	private static final String FILM_TYPES_VALUES = "wd:Q11424 wd:Q24869 wd:Q202860";

	private final WikidataClient client;
	private final WikidataProperties properties;
	private final WikidataNeighborCache neighborCache;

	public WikidataService(
			WikidataClient client,
			WikidataProperties properties,
			WikidataNeighborCache neighborCache) {
		this.client = client;
		this.properties = properties;
		this.neighborCache = neighborCache;
	}

	public WikidataNode resolveNode(String qid) {
		WikidataNode actor = queryHuman(qid);
		if (actor != null) {
			return actor;
		}
		WikidataNode film = queryFilm(qid);
		if (film != null) {
			return film;
		}
		String instanceOf = describeInstanceOf(qid);
		if (instanceOf == null) {
			throw new InvalidMoveException("Entity is not a film or human: " + qid);
		}
		throw new InvalidMoveException(
				"Entity is not a film or human: " + qid + " (instance of: " + instanceOf + ")");
	}

	public boolean areAdjacent(String fromId, String toId) {
		String sparql = """
				ASK {
				  BIND(%s AS ?from)
				  BIND(%s AS ?to)
				  %s
				}
				""".formatted(WikidataClient.wd(fromId), WikidataClient.wd(toId), castLinkConnecting("?from", "?to"));
		return client.ask(sparql);
	}

	public List<WikidataNode> getNeighbors(WikidataNode current) {
		// Always resolve type from Wikidata so stale Redis state cannot break neighbor queries.
		WikidataNode node = resolveNode(current.id());
		return neighborCache.get(node.id()).map(this::ensureDisplayableLabels).orElseGet(() -> {
			List<WikidataNode> neighbors = ensureDisplayableLabels(fetchNeighbors(node));
			neighborCache.put(node.id(), neighbors);
			return neighbors;
		});
	}

	private List<WikidataNode> ensureDisplayableLabels(List<WikidataNode> nodes) {
		return WikidataLabelEnricher.enrich(nodes, client::fetchEntityLabels);
	}

	private List<WikidataNode> fetchNeighbors(WikidataNode node) {
		int limit = properties.getNeighborLimit();
		if (node.type() == NodeType.ACTOR) {
			String sparql = """
					SELECT DISTINCT ?film ?filmLabel WHERE {
					  BIND(%s AS ?actor)
					  %s
					  %s .
					  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
					}
					LIMIT %d
					""".formatted(
					WikidataClient.wd(node.id()),
					castLinkBetween("?film", "?actor"),
					filmTypeClause(),
					limit);
			return client.selectNodes(sparql, "film", "filmLabel", NodeType.FILM);
		}

		String sparql = """
				SELECT DISTINCT ?actor ?actorLabel WHERE {
				  BIND(%s AS ?film)
				  %s
				  ?actor wdt:P31/wdt:P279* wd:%s .
				  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
				}
				LIMIT %d
				""".formatted(
				WikidataClient.wd(node.id()),
				castLinkBetween("?film", "?actor"),
				HUMAN,
				limit);
		return client.selectNodes(sparql, "actor", "actorLabel", NodeType.ACTOR);
	}

	public List<FilmSearchResult> searchFilms(String query) {
		if (query == null || query.isBlank()) {
			return List.of();
		}
		String trimmed = query.trim();
		if (trimmed.length() < 2) {
			return List.of();
		}

		List<FilmSearchResult> candidates = fetchSearchCandidates(trimmed);
		Map<String, FilmSearchResult> unique = new LinkedHashMap<>();
		for (FilmSearchResult candidate : candidates) {
			if (unique.size() >= properties.getSearchResultLimit()) {
				break;
			}
			WikidataNode film = queryFilm(candidate.id());
			if (film != null) {
				unique.putIfAbsent(
						film.id(),
						new FilmSearchResult(film.id(), film.label(), candidate.description()));
			}
		}
		return List.copyOf(unique.values());
	}

	private List<FilmSearchResult> fetchSearchCandidates(String trimmed) {
		try {
			return client.searchEntities(trimmed, properties.getSearchCandidateLimit());
		}
		catch (WikidataUnavailableException ex) {
			return client.searchFilmsByLabel(trimmed, properties.getSearchCandidateLimit());
		}
	}

	public WikidataNode resolveFilm(String qid) {
		WikidataNode film = queryFilm(qid);
		if (film == null) {
			String instanceOf = describeInstanceOf(qid);
			if (instanceOf == null) {
				throw new InvalidMoveException("Entity is not a film: " + qid);
			}
			throw new InvalidMoveException("Entity is not a film: " + qid + " (instance of: " + instanceOf + ")");
		}
		return film;
	}

	public void requireAlternatingType(NodeType fromType, NodeType toType) {
		NodeType expected = fromType == NodeType.FILM ? NodeType.ACTOR : NodeType.FILM;
		if (toType != expected) {
			String step = fromType == NodeType.FILM ? "pick a cast member" : "pick a film from filmography";
			throw new InvalidMoveException("From " + fromType + " you must " + step + ", not " + toType);
		}
	}

	private WikidataNode queryHuman(String qid) {
		String sparql = """
				SELECT ?item ?itemLabel WHERE {
				  BIND(%s AS ?item)
				  ?item wdt:P31/wdt:P279* wd:%s .
				  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
				}
				LIMIT 1
				""".formatted(WikidataClient.wd(qid), HUMAN);
		return client.selectSingleNode(sparql, "item", "itemLabel", NodeType.ACTOR);
	}

	private WikidataNode queryFilm(String qid) {
		String sparql = """
				SELECT ?item ?itemLabel WHERE {
				  BIND(%s AS ?item)
				  ?item wdt:P31/wdt:P279* ?filmType .
				  VALUES ?filmType { %s }
				  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
				}
				LIMIT 1
				""".formatted(WikidataClient.wd(qid), FILM_TYPES_VALUES);
		return client.selectSingleNode(sparql, "item", "itemLabel", NodeType.FILM);
	}

	private String describeInstanceOf(String qid) {
		String sparql = """
				SELECT ?typeLabel WHERE {
				  %s wdt:P31 ?type .
				  SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
				}
				LIMIT 1
				""".formatted(WikidataClient.wd(qid));
		return client.selectLabel(sparql, "typeLabel");
	}

	private static String filmTypeClause() {
		return "?film wdt:P31/wdt:P279* ?filmType . VALUES ?filmType { " + FILM_TYPES_VALUES + " }";
	}

	/**
	 * Cast member (P161) or voice actor (P725) — animated films such as Cars (Q182153) often
	 * only list P725.
	 */
	private static String castLinkBetween(String filmVar, String personVar) {
		return """
				{ %s wdt:P161 %s . } UNION
				{ %s wdt:P725 %s . }
				""".formatted(filmVar, personVar, filmVar, personVar);
	}

	private static String castLinkConnecting(String leftVar, String rightVar) {
		return castLinkBetween(leftVar, rightVar) + " UNION " + castLinkBetween(rightVar, leftVar);
	}

}
