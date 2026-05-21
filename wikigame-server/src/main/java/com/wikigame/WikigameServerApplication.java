package com.wikigame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.wikigame.config.WikigameProperties;
import com.wikigame.wikidata.WikidataProperties;

@SpringBootApplication
@EnableConfigurationProperties({ WikigameProperties.class, WikidataProperties.class })
public class WikigameServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WikigameServerApplication.class, args);
	}

}
