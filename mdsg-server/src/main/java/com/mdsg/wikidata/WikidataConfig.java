package com.mdsg.wikidata;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class WikidataConfig {

	@Bean
	RestClient wikidataRestClient(WikidataProperties properties) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(properties.getTimeout());
		factory.setReadTimeout(properties.getTimeout());

		return RestClient.builder()
			.requestFactory(factory)
			.defaultHeader("User-Agent", properties.getUserAgent())
			.build();
	}

}
