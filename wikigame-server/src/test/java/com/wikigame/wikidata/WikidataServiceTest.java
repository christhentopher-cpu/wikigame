package com.wikigame.wikidata;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.wikigame.domain.NodeType;
import com.wikigame.domain.WikidataNode;

@SpringBootTest
class WikidataServiceTest {

	@Autowired
	private WikidataService wikidataService;

	@Test
	void carsFilmIncludesVoiceCastNeighbors() {
		WikidataNode cars = new WikidataNode("Q182153", "Cars", NodeType.FILM);

		var neighbors = wikidataService.getNeighbors(cars);

		assertThat(neighbors).isNotEmpty();
		assertThat(neighbors).anyMatch(node -> node.type() == NodeType.ACTOR);
	}

	@Test
	void voiceActorCanMoveBackToAnimatedFilm() {
		assertThat(wikidataService.areAdjacent("Q161916", "Q182153")).isTrue();
	}

}
