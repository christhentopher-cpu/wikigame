package com.wikigame.wikidata;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wikigame.wikidata")
public class WikidataProperties {

	private String sparqlUrl = "https://query.wikidata.org/sparql";
	private String apiUrl = "https://www.wikidata.org/w/api.php";
	private String userAgent = "wikigame-server/0.1 (local-dev)";
	private Duration timeout = Duration.ofSeconds(15);
	private int neighborLimit = 200;
	private int searchCandidateLimit = 20;
	private int searchResultLimit = 8;
	private int retryMaxAttempts = 3;
	private long retryBackoffMs = 500;

	public String getSparqlUrl() {
		return sparqlUrl;
	}

	public void setSparqlUrl(String sparqlUrl) {
		this.sparqlUrl = sparqlUrl;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public Duration getTimeout() {
		return timeout;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public int getNeighborLimit() {
		return neighborLimit;
	}

	public void setNeighborLimit(int neighborLimit) {
		this.neighborLimit = neighborLimit;
	}

	public int getSearchCandidateLimit() {
		return searchCandidateLimit;
	}

	public void setSearchCandidateLimit(int searchCandidateLimit) {
		this.searchCandidateLimit = searchCandidateLimit;
	}

	public int getSearchResultLimit() {
		return searchResultLimit;
	}

	public void setSearchResultLimit(int searchResultLimit) {
		this.searchResultLimit = searchResultLimit;
	}

	public int getRetryMaxAttempts() {
		return retryMaxAttempts;
	}

	public void setRetryMaxAttempts(int retryMaxAttempts) {
		this.retryMaxAttempts = retryMaxAttempts;
	}

	public long getRetryBackoffMs() {
		return retryBackoffMs;
	}

	public void setRetryBackoffMs(long retryBackoffMs) {
		this.retryBackoffMs = retryBackoffMs;
	}

}
