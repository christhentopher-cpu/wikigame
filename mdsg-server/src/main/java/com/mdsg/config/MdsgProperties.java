package com.mdsg.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mdsg")
public class MdsgProperties {

	private final Game game = new Game();
	private final Cors cors = new Cors();
	private final Websocket websocket = new Websocket();

	public Game getGame() {
		return game;
	}

	public Cors getCors() {
		return cors;
	}

	public Websocket getWebsocket() {
		return websocket;
	}

	public static class Game {
		private Duration ttl = Duration.ofHours(24);

		public Duration getTtl() {
			return ttl;
		}

		public void setTtl(Duration ttl) {
			this.ttl = ttl;
		}
	}

	public static class Cors {
		private List<String> allowedOriginPatterns = List.of(
				"http://localhost:*",
				"http://127.0.0.1:*",
				"http://192.168.*.*:*",
				"http://10.*.*.*:*");

		public List<String> getAllowedOriginPatterns() {
			return allowedOriginPatterns;
		}

		public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
			this.allowedOriginPatterns = allowedOriginPatterns;
		}
	}

	public static class Websocket {
		private String endpoint = "/ws";
		private String applicationPrefix = "/app";
		private String brokerPrefix = "/topic";
		private String gameTopicPrefix = "/topic/game";

		public String getEndpoint() {
			return endpoint;
		}

		public void setEndpoint(String endpoint) {
			this.endpoint = endpoint;
		}

		public String getApplicationPrefix() {
			return applicationPrefix;
		}

		public void setApplicationPrefix(String applicationPrefix) {
			this.applicationPrefix = applicationPrefix;
		}

		public String getBrokerPrefix() {
			return brokerPrefix;
		}

		public void setBrokerPrefix(String brokerPrefix) {
			this.brokerPrefix = brokerPrefix;
		}

		public String getGameTopicPrefix() {
			return gameTopicPrefix;
		}

		public void setGameTopicPrefix(String gameTopicPrefix) {
			this.gameTopicPrefix = gameTopicPrefix;
		}
	}

}
